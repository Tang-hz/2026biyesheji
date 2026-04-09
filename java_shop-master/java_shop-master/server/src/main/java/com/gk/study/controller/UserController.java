package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.User;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "用户管理控制层")
@RestController
@RequestMapping("/user")
public class UserController {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    String salt = "abcd1234";

    @Autowired
    UserService userService;

    @Value("${File.uploadPath}")
    private String uploadPath;

    @Operation(summary = "用户列表（关键词）")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(String keyword){
        List<User> list =  userService.getUserList(keyword);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    @Operation(summary = "用户详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public APIResponse detail(String userId){
        User user =  userService.getUserDetail(userId);
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", user);
    }

    @Operation(summary = "管理员登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public APIResponse login(User user){
        String rawPassword = user.getPassword();
        user.setPassword(DigestUtils.md5DigestAsHex((rawPassword + salt).getBytes()));
        User responseUser = userService.getAdminUser(user);
        // Backward compatibility: if admin password was manually changed to plaintext in DB,
        // allow one login and normalize it back to salted-MD5 storage.
        if (responseUser == null) {
            User adminUser = userService.getAdminUserByUsername(user.getUsername());
            if (adminUser != null && rawPassword.equals(adminUser.getPassword())) {
                adminUser.setPassword(DigestUtils.md5DigestAsHex((rawPassword + salt).getBytes()));
                adminUser.setToken(DigestUtils.md5DigestAsHex((adminUser.getUsername() + salt).getBytes()));
                userService.updateUser(adminUser);
                responseUser = adminUser;
            }
        }
        if(responseUser != null) {
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", responseUser);
        }else {
            return new APIResponse(ResponeCode.FAIL, "用户名或密码错误");
        }
    }

    @Operation(summary = "普通用户登录")
    @RequestMapping(value = "/userLogin", method = RequestMethod.POST)
    public APIResponse userLogin(User user){
        user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + salt).getBytes()));
        User responseUser =  userService.getNormalUser(user);
        if(responseUser != null) {
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", responseUser);
        }else {
            return new APIResponse(ResponeCode.FAIL, "用户名或密码错误");
        }
    }

    @Operation(summary = "普通用户注册")
    @RequestMapping(value = "/userRegister", method = RequestMethod.POST)
    @Transactional
    public APIResponse userRegister(User user) throws IOException {

        if (!StringUtils.isEmpty(user.getUsername())
                && !StringUtils.isEmpty(user.getPassword())
                && !StringUtils.isEmpty(user.getRePassword())) {
            // 查重
            if(userService.getUserByUserName(user.getUsername()) != null) {
                return new APIResponse(ResponeCode.FAIL, "用户名重复");
            }
            // 验证密码
            if(!user.getPassword().equals(user.getRePassword())) {
                return new APIResponse(ResponeCode.FAIL, "密码不一致");
            }
            String md5Str = DigestUtils.md5DigestAsHex((user.getPassword() + salt).getBytes());
            // 设置密码
            user.setPassword(md5Str);
            md5Str = DigestUtils.md5DigestAsHex((user.getUsername() + salt).getBytes());
            // 设置token
            user.setToken(md5Str);

            String avatar = saveAvatar(user);
            if(!StringUtils.isEmpty(avatar)) {
                user.avatar = avatar;
            }
            // 设置角色
            user.setRole(String.valueOf(User.NormalUser));
            // 设置状态
            user.setStatus("0");
            user.setCreateTime(java.time.LocalDateTime.now());

            userService.createUser(user);
            return new APIResponse(ResponeCode.SUCCESS, "创建成功");
        }
        return new APIResponse(ResponeCode.FAIL, "创建失败");
    }

    @Operation(summary = "新增用户（管理员）")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(User user) throws IOException {

        if (!StringUtils.isEmpty(user.getUsername()) || !StringUtils.isEmpty(user.getPassword())) {
            // 查重
            if(userService.getUserByUserName(user.getUsername()) != null) {
                return new APIResponse(ResponeCode.FAIL, "用户名重复");
            }
            String md5Str = DigestUtils.md5DigestAsHex((user.getPassword() + salt).getBytes());
            // 设置密码
            user.setPassword(md5Str);
            md5Str = DigestUtils.md5DigestAsHex((user.getUsername() + salt).getBytes());
            // 设置token
            user.setToken(md5Str);
            user.setCreateTime(java.time.LocalDateTime.now());

            String avatar = saveAvatar(user);
            if(!StringUtils.isEmpty(avatar)) {
                user.avatar = avatar;
            }
            userService.createUser(user);
            return new APIResponse(ResponeCode.SUCCESS, "创建成功");
        }
        return new APIResponse(ResponeCode.FAIL, "创建失败");
    }

    @Operation(summary = "批量删除用户（管理员）")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            userService.deleteUser(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    @Operation(summary = "更新用户（管理员）")
    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(User user) throws IOException {
        // update不能修改密码，故置空
        user.setPassword(null);
        String avatar = saveAvatar(user);
        if(!StringUtils.isEmpty(avatar)) {
            user.avatar = avatar;
        }
        userService.updateUser(user);
        System.out.println(user);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }


    @Operation(summary = "更新当前用户资料（普通用户）")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    @Transactional
    public APIResponse updateUserInfo(User user) throws IOException {
        User tmpUser =  userService.getUserDetail(user.getId());
        if(tmpUser.getRole().equals(String.valueOf(User.NormalUser))){
            // username和password不能改，故置空
            user.setUsername(null);
            user.setPassword(null);
            user.setRole(String.valueOf(User.NormalUser));
            String avatar = saveAvatar(user);
            if(!StringUtils.isEmpty(avatar)) {
                user.avatar = avatar;
            }
            userService.updateUser(user);
            return new APIResponse(ResponeCode.SUCCESS, "更新成功");
        }else {
            return new APIResponse(ResponeCode.FAIL, "非法操作");
        }
    }

    @Operation(summary = "修改用户名（普通用户，需验证密码）")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/updateUsername", method = RequestMethod.POST)
    @Transactional
    public APIResponse updateUsername(String userId, String password, String newUsername) {
        if (!StringUtils.hasText(newUsername)) {
            return new APIResponse(ResponeCode.FAIL, "用户名不能为空");
        }
        String name = newUsername.trim();
        if (name.length() < 2 || name.length() > 32) {
            return new APIResponse(ResponeCode.FAIL, "用户名长度为2-32个字符");
        }
        User user = userService.getUserDetail(userId);
        if (user == null || !user.getRole().equals(String.valueOf(User.NormalUser))) {
            return new APIResponse(ResponeCode.FAIL, "非法操作");
        }
        if (name.equals(user.getUsername())) {
            return new APIResponse(ResponeCode.FAIL, "新用户名与当前相同");
        }
        User taken = userService.getUserByUserName(name);
        if (taken != null && !taken.getId().equals(user.getId())) {
            return new APIResponse(ResponeCode.FAIL, "用户名已被占用");
        }
        String md5Pwd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        if (!user.getPassword().equals(md5Pwd)) {
            return new APIResponse(ResponeCode.FAIL, "密码错误");
        }
        user.setUsername(name);
        user.setToken(DigestUtils.md5DigestAsHex((name + salt).getBytes()));
        userService.updateUser(user);
        user.setPassword(null);
        return new APIResponse(ResponeCode.SUCCESS, "修改成功", user);
    }

    @Operation(summary = "修改密码（普通用户）")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/updatePwd", method = RequestMethod.POST)
    @Transactional
    public APIResponse updatePwd(String userId, String password, String newPassword) throws IOException {
        User user =  userService.getUserDetail(userId);
        if(user.getRole().equals(String.valueOf(User.NormalUser))) {
            String md5Pwd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(user.getPassword().equals(md5Pwd)){
                user.setPassword(DigestUtils.md5DigestAsHex((newPassword + salt).getBytes()));
                userService.updateUser(user);
            }else {
                return new APIResponse(ResponeCode.FAIL, "原密码错误");
            }
            return new APIResponse(ResponeCode.SUCCESS, "更新成功");
        }else {
            return new APIResponse(ResponeCode.FAIL, "非法操作");
        }

    }

    public String saveAvatar(User user) throws IOException {
        MultipartFile file = user.getAvatarFile();
        String newFileName = null;
        if(file !=null && !file.isEmpty()) {

            // 存文件
            String oldFileName = file.getOriginalFilename();
            String randomStr = UUID.randomUUID().toString();
            newFileName = randomStr + oldFileName.substring(oldFileName.lastIndexOf("."));
            String filePath = uploadPath + File.separator + "avatar" + File.separator + newFileName;
            File destFile = new File(filePath);
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);
        }
        if(!StringUtils.isEmpty(newFileName)) {
            user.avatar = newFileName;
        }
        return newFileName;
    }
}
