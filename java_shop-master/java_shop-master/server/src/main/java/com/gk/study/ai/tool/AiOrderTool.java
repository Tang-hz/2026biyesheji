package com.gk.study.ai.tool;

import com.gk.study.entity.Address;
import com.gk.study.entity.Order;
import com.gk.study.entity.Thing;
import com.gk.study.service.AddressService;
import com.gk.study.service.OrderService;
import com.gk.study.service.ThingService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 客服工具（Function Calling）：商品检索、订单查询、下单。
 */
@Component
public class AiOrderTool {

    private final ThingService thingService;
    private final OrderService orderService;
    private final AddressService addressService;

    public AiOrderTool(ThingService thingService,
                        OrderService orderService,
                        AddressService addressService) {
        this.thingService = thingService;
        this.orderService = orderService;
        this.addressService = addressService;
    }

    private static final int SEARCH_RESULT_LIMIT = 8;

    @Tool("用户想确认某商品是否在架、问「有没有某某商品」、按类目/标签（如「服装」「衣服」）浏览时调用。参数为搜索关键词；会同时匹配商品标题与后台标签名称（常见口语如「衣服」会关联「服装」等标签），返回若干条摘要，禁止编造列表外的商品。")
    public String searchThingsByKeyword(
            @P("商品名称、类目或口语词，支持模糊匹配，例如「奶茶」「蓝牙」「衣服」「服装」。")
            String keyword
    ) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return "请告诉我你要搜索的商品名称、类目或关键词，我再帮你查。";
        }
        List<Thing> raw = thingService.searchThingsByTitleOrTag(kw);
        if (raw == null || raw.isEmpty()) {
            return "没有找到与「" + kw + "」相匹配的商品，请换一个关键词试试。";
        }
        int n = Math.min(raw.size(), SEARCH_RESULT_LIMIT);
        List<Thing> slice = raw.subList(0, n);
        StringBuilder sb = new StringBuilder();
        sb.append("共找到至少 ").append(raw.size()).append(" 条相关商品，为您列出前 ").append(n).append(" 条。\n");
        sb.append("下单说明：优先使用「商品ID」调用按ID下单工具最准确；或使用下列「标题」原文调用按标题下单（勿带星号等 Markdown）。\n");
        for (int i = 0; i < slice.size(); i++) {
            Thing t = slice.get(i);
            if (t == null) {
                continue;
            }
            sb.append(i + 1).append(") ");
            sb.append("标题：").append(nullToEmpty(t.getTitle()));
            sb.append("；商品ID：").append(t.getId() == null ? "-" : String.valueOf(t.getId()));
            sb.append("；价格：").append(nullToEmpty(t.getPrice() == null ? null : t.getPrice().toString()));
            String stock = nullToEmpty(t.getRepertory());
            if (!stock.isEmpty()) {
                sb.append("；库存参考：").append(stock);
            }
            sb.append("\n");
        }
        if (raw.size() > n) {
            sb.append("若有多条都可能符合，请让用户确认要买哪一条（说序号、完整标题或商品ID）。");
        }
        return sb.toString().trim();
    }

    @Tool("用户已提供订单号、要查订单状态/详情时调用。仅返回该登录用户本人订单；订单号须与用户提供的一致。未登录无法查询。")
    public String getUserOrderByOrderNumber(
            @P("用户提供的订单号，应与订单详情中显示的一致。")
            String orderNumber,

            @ToolMemoryId
            String userId
    ) {
        String on = orderNumber == null ? "" : orderNumber.trim();
        if (on.isEmpty()) {
            return "还没有订单号。请用户提供订单号后，我再帮您查。";
        }
        String uid = userId == null ? "" : userId.trim();
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "查询订单需要您先登录账号；登录后我才能凭订单号为您核实。"
                    + "您也可以在【我的订单】里查看订单状态。";
        }
        List<Order> orders = orderService.getUserOrderList(uid, null);
        if (orders == null || orders.isEmpty()) {
            return "当前账号下暂时没有订单记录。若刚下单，可稍后在【我的订单】刷新查看。"
                    + "订单号请核对是否与「" + on + "」一致。";
        }
        Order hit = null;
        for (Order o : orders) {
            if (o != null && o.getOrderNumber() != null && on.equals(o.getOrderNumber().trim())) {
                hit = o;
                break;
            }
        }
        if (hit == null) {
            return "在您当前账号下没有找到订单号为「" + on + "」的订单。"
                    + "请核对订单号是否复制完整，或是否登录了下单时使用的账号。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("已查到订单：订单号 ").append(hit.getOrderNumber());
        sb.append("；状态：").append(orderStatusLabel(hit.getStatus()));
        sb.append("；商品：").append(nullToEmpty(hit.getTitle()));
        sb.append("；数量：").append(nullToEmpty(hit.getCount()));
        sb.append("；单价参考：¥").append(nullToEmpty(hit.getPrice()));
        String time = formatOrderTime(hit.getOrderTime());
        if (!time.isEmpty()) {
            sb.append("；下单时间：").append(time);
        }
        if (hit.getReceiverAddress() != null && !hit.getReceiverAddress().isBlank()) {
            sb.append("。收货地址已在系统中登记（详情请到订单页查看）。");
        }
        return sb.toString();
    }

    @Tool("用户已提供检索结果中的数字「商品ID」、或粘贴含 ID 的片段时要下单，调用本工具。比按标题匹配更可靠。用户只说「帮我下单」且上一轮检索仅一条时，用该条的商品ID调用本工具。")
    public String orderByThingId(
            @P("商品主键 ID，纯数字；可从上一轮检索结果的「商品ID：」后复制，或从用户粘贴内容中解析。")
            String thingId,

            @P(value = "数量，可选；不提供或非法则默认 1。", required = false)
            Integer count,

            @P(value = "备注信息，可选。", required = false)
            String remark,

            @ToolMemoryId
            String userId
    ) {
        String idStr = parseThingIdString(thingId);
        if (idStr.isEmpty()) {
            return "我没识别到有效的商品ID（应为数字）。请让用户从检索列表里复制「商品ID」后的数字，或再说一次商品。";
        }

        String uid = userId == null ? "" : userId.trim();
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "请先登录后再下单。";
        }

        Thing thing = thingService.selectThingById(idStr);
        if (thing == null) {
            return "商品ID「" + idStr + "」在系统中不存在，请核对是否复制正确或重新检索商品。";
        }

        int quantity = (count == null || count < 1) ? 1 : count;
        String finalRemark = (remark == null || remark.trim().isEmpty()) ? null : remark.trim();

        return placeOrder(thing, uid, quantity, finalRemark);
    }

    @Tool("用户要按「商品标题/名称」下单时调用（用户粘贴了完整标题或未提供 ID 时）。若上一轮已检索出唯一一条，用户说「帮我下单」时也可把该条完整标题传入。有商品ID时优先用按ID下单工具。")
    public String orderByThingTitle(
            @P("商品标题：尽量与检索结果中「标题：」后的原文一致；可从对话上文最近一次检索工具返回值中复制，不要加 ** 等符号。")
            String thingTitle,

            @P(value = "数量，可选；不提供或非法则默认 1。", required = false)
            Integer count,

            @P(value = "备注信息，可选。", required = false)
            String remark,

            @ToolMemoryId
            String userId
    ) {
        String normalizedTitle = normalizeTitle(thingTitle);
        if (normalizedTitle.isBlank()) {
            return "我没理解到你要下单的商品名称。请再告诉我一次「商品名称」或「商品ID」。";
        }

        String uid = userId == null ? "" : userId.trim();
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "请先登录后再下单。";
        }

        int quantity = (count == null || count < 1) ? 1 : count;
        String finalRemark = (remark == null || remark.trim().isEmpty()) ? null : remark.trim();

        Thing thing = findBestThingByTitle(normalizedTitle);
        if (thing == null) {
            return "我没在商品列表里找到与「" + normalizedTitle + "」匹配的商品。请提供更准确的标题，或使用检索结果中的「商品ID」下单。";
        }

        return placeOrder(thing, uid, quantity, finalRemark);
    }

    private String placeOrder(Thing thing, String uid, int quantity, String finalRemark) {
        List<Address> addresses = addressService.getAddressList(uid);
        if (addresses == null || addresses.isEmpty()) {
            return "你还没有收货地址。请先去【地址管理】新增地址后再下单。";
        }

        Address receiver = pickDefaultAddress(addresses);
        if (receiver == null) {
            receiver = addresses.get(0);
        }

        Order order = new Order();
        order.setThingId(String.valueOf(thing.getId()));
        order.setUserId(uid);
        order.setCount(String.valueOf(quantity));
        order.setReceiverName(receiver.getName());
        order.setReceiverPhone(receiver.getMobile());
        order.setReceiverAddress(receiver.getDescription());
        if (finalRemark != null) {
            order.setRemark(finalRemark);
        }

        orderService.createOrder(order);

        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            return "下单成功，但订单号生成失败，请稍后重试。";
        }
        return "已为你下单《" + nullToEmpty(thing.getTitle()) + "》，订单号：" + order.getOrderNumber();
    }

    private Thing findBestThingByTitle(String normalizedTitle) {
        List<Thing> candidates = thingService.searchThingsByTitleOrTag(normalizedTitle);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (Thing t : candidates) {
            if (t == null || t.getTitle() == null) continue;
            if (normalizeTitle(t.getTitle()).equalsIgnoreCase(normalizedTitle)) {
                return t;
            }
        }

        for (Thing t : candidates) {
            if (t == null || t.getTitle() == null) continue;
            String nt = normalizeTitle(t.getTitle());
            if (nt.contains(normalizedTitle) || normalizedTitle.contains(nt)) {
                return t;
            }
        }

        return candidates.get(0);
    }

    private Address pickDefaultAddress(List<Address> addresses) {
        for (Address a : addresses) {
            if (a == null) continue;
            if ("1".equals(a.getDef())) {
                return a;
            }
        }
        return null;
    }

    /**
     * 从用户或模型传入的字符串中提取商品 ID（纯数字），取长数字串避免误取数量「2」等。
     */
    private static String parseThingIdString(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String t = raw.trim().replaceAll("[*`_#]", "");
        if (t.matches("\\d+")) {
            return t;
        }
        String best = "";
        Matcher m = Pattern.compile("\\d+").matcher(t);
        while (m.find()) {
            String g = m.group();
            if (g.length() >= best.length()) {
                best = g;
            }
        }
        return best;
    }

    private static String normalizeTitle(String title) {
        if (title == null) return "";
        String t = title.trim();
        t = t.replaceAll("[《》\"“”‘’'\\[\\]（）(){}]", "");
        // 去掉 Markdown 常见符号，避免 **标题** 导致匹配失败
        t = t.replaceAll("[*`_#]+", "");
        t = t.replaceAll("\\s+", "");
        return t;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String orderStatusLabel(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case "1" -> "待支付";
            case "2" -> "已支付";
            default -> "已取消或其他（状态码 " + status + "）";
        };
    }

    private static String formatOrderTime(java.time.LocalDateTime orderTime) {
        if (orderTime == null) {
            return "";
        }
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(orderTime);
    }
}
