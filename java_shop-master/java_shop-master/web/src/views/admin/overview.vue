<template>
  <a-spin :spinning="showSpin">
    <div class="main">
      <!-- 统计卡片行（保持不变） -->
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="商品总数">
            <template #extra><a-tag color="blue">总</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.spzs }}<span class="v-e">种</span></span>
              </div>
              <div class="box-bottom"><span>本周新增 {{ tdata.data.qrxz }} 种</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="未付订单">
            <template #extra><a-tag color="green">未付</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.wfdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.wfddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="已付订单">
            <template #extra><a-tag color="blue">已付</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.yfdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.yfddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="取消订单">
            <template #extra><a-tag color="red">取消</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.qxdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.qxddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 保留图表：最近一周访问量 -->
      <a-card title="最近一周访问量" class="chart-card">
        <div style="height: 280px;" ref="visitChartDiv"></div>
      </a-card>

      <!-- 保留图表：热门商品 + 热门分类 -->
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="热门商品排名" class="chart-card">
            <div style="height: 280px;" ref="barChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="热门分类比例" class="chart-card">
            <div style="height: 280px;" ref="pieChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- ===== 新增图表区域 ===== -->

      <!-- 用户分析区 -->
      <div class="section-title">用户分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="用户增长趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="userGrowthType" size="small" @change="loadUserGrowth">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="userGrowthChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="会员等级分布" class="chart-card">
            <div style="height: 260px;" ref="memberDistChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 销售分析区 -->
      <div class="section-title">销售分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="销售额趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="salesTrendType" size="small" @change="loadSalesTrend">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="salesTrendChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="客单价分布" class="chart-card">
            <div style="height: 260px;" ref="avgOrderChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 订单分析区 -->
      <div class="section-title">订单分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="订单状态分布" class="chart-card">
            <div style="height: 260px;" ref="orderStatusChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="订单量趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="orderTrendType" size="small" @change="loadOrderTrend">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="orderTrendChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 商品分析区 -->
      <div class="section-title">商品分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="商品评分分布" class="chart-card">
            <div style="height: 260px;" ref="thingScoreChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="商品热度 TOP10" class="chart-card">
            <div style="height: 260px;" ref="thingHotChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

    </div>
  </a-spin>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue';
import {listApi, userGrowthApi, memberDistributionApi, salesTrendApi,
        avgOrderValueApi, orderStatusApi, orderTrendApi,
        thingScoreApi, thingHotApi} from '/@/api/overview';

let showSpin = ref(true);

// 时间切换类型
const userGrowthType = ref('day');
const salesTrendType = ref('day');
const orderTrendType = ref('day');

// 图表 ref
const visitChartDiv = ref();
const barChartDiv = ref();
const pieChartDiv = ref();
const userGrowthChartDiv = ref();
const memberDistChartDiv = ref();
const salesTrendChartDiv = ref();
const avgOrderChartDiv = ref();
const orderStatusChartDiv = ref();
const orderTrendChartDiv = ref();
const thingScoreChartDiv = ref();
const thingHotChartDiv = ref();

// 图表实例
let visitChart: any, barChart: any, pieChart: any;
let userGrowthChart: any, memberDistChart: any;
let salesTrendChart: any, avgOrderChart: any;
let orderStatusChart: any, orderTrendChart: any;
let thingScoreChart: any, thingHotChart: any;

let tdata = reactive({data: {} as any});

onMounted(() => {
  listApi({}).then(res => {
    tdata.data = res.data;
    initCharts();
    initBarChart();
    initPieChart();
    loadNewCharts();
    showSpin.value = false;
  }).catch(() => {
    showSpin.value = false;
  });

  window.onresize = () => {
    visitChart?.resize();
    barChart?.resize();
    pieChart?.resize();
    userGrowthChart?.resize();
    memberDistChart?.resize();
    salesTrendChart?.resize();
    avgOrderChart?.resize();
    orderStatusChart?.resize();
    orderTrendChart?.resize();
    thingScoreChart?.resize();
    thingHotChart?.resize();
  };
});

// 加载所有新图表
const loadNewCharts = () => {
  loadUserGrowth();
  loadMemberDistribution();
  loadSalesTrend();
  loadAvgOrderValue();
  loadOrderStatus();
  loadOrderTrend();
  loadThingScore();
  loadThingHot();
};

// 用户增长趋势
const loadUserGrowth = () => {
  userGrowthApi({type: userGrowthType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.count);
    userGrowthChart = echarts.init(userGrowthChartDiv.value);
    userGrowthChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '新增用户', type: 'line', data: yData, itemStyle: {color: '#36CBCB'}}]
    });
  });
};

// 会员等级分布
const loadMemberDistribution = () => {
  memberDistributionApi({}).then(res => {
    const data = res.data.map((d: any) => ({name: d.level, value: d.count}));
    memberDistChart = echarts.init(memberDistChartDiv.value);
    memberDistChart.setOption({
      tooltip: {trigger: 'item'},
      legend: {bottom: 10, left: 'center'},
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {normal: {color: (params: any) => ['#70B0EA', '#B3A3DA', '#F6BD3C', '#F76B4C'][params.dataIndex]}},
        label: {show: false, position: 'center'},
        emphasis: {label: {show: true, fontSize: 18, fontWeight: 'bold'}},
        data
      }]
    });
  });
};

// 销售额趋势
const loadSalesTrend = () => {
  salesTrendApi({type: salesTrendType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.amount);
    salesTrendChart = echarts.init(salesTrendChartDiv.value);
    salesTrendChart.setOption({
      tooltip: {trigger: 'axis', formatter: (params: any) => `${params[0].name}<br/>销售额: ¥${params[0].value.toFixed(2)}`},
      grid: {top: 30, left: 60, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}, label: {formatter: '¥{value}'}},
      series: [{name: '销售额', type: 'line', data: yData, itemStyle: {color: '#F6BD3C'}, areaStyle: {color: 'rgba(246,189,60,0.2)'}}]
    });
  });
};

// 客单价分布
const loadAvgOrderValue = () => {
  avgOrderValueApi({}).then(res => {
    const xData = res.data.map((d: any) => d.range);
    const yData = res.data.map((d: any) => d.count);
    avgOrderChart = echarts.init(avgOrderChartDiv.value);
    avgOrderChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 60, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}, name: '价格区间(元)'},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '订单数', type: 'bar', data: yData, itemStyle: {color: '#F76B4C'}}]
    });
  });
};

// 订单状态分布
const loadOrderStatus = () => {
  orderStatusApi({}).then(res => {
    const data = res.data.map((d: any) => ({name: d.status, value: d.count}));
    orderStatusChart = echarts.init(orderStatusChartDiv.value);
    orderStatusChart.setOption({
      tooltip: {trigger: 'item'},
      legend: {bottom: 10, left: 'center'},
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        itemStyle: {normal: {color: (params: any) => ['#90949A', '#F6BD3C', '#4ECB73', '#F76B4C'][params.dataIndex]}},
        label: {show: false},
        emphasis: {label: {show: true, fontSize: 16, fontWeight: 'bold'}},
        data
      }]
    });
  });
};

// 订单量趋势
const loadOrderTrend = () => {
  orderTrendApi({type: orderTrendType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.count);
    orderTrendChart = echarts.init(orderTrendChartDiv.value);
    orderTrendChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '订单量', type: 'line', data: yData, itemStyle: {color: '#4ECB73'}}]
    });
  });
};

// 商品评分分布
const loadThingScore = () => {
  thingScoreApi({}).then(res => {
    const xData = res.data.map((d: any) => d.score);
    const yData = res.data.map((d: any) => d.count);
    thingScoreChart = echarts.init(thingScoreChartDiv.value);
    thingScoreChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}, name: '评分'},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '商品数', type: 'bar', data: yData, itemStyle: {color: '#70B0EA'}}]
    });
  });
};

// 商品热度 TOP10
const loadThingHot = () => {
  thingHotApi({}).then(res => {
    const xData = res.data.map((d: any) => d.title);
    const yData = res.data.map((d: any) => d.pv);
    thingHotChart = echarts.init(thingHotChartDiv.value);
    thingHotChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 100, right: 20, bottom: 30},
      xAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      yAxis: {type: 'category', data: xData.reverse(), axisLabel: {color: '#666'}},
      series: [{name: '浏览量', type: 'bar', data: yData.reverse(), itemStyle: {color: '#B3A3DA'}}]
    });
  });
};

// 以下为原有图表方法（保持不变）
const initCharts = () => {
  let xData: any[] = [], uvData: any[] = [], pvData: any[] = [];
  tdata.data.visitList?.forEach((item: any) => {
    xData.push(item.day); uvData.push(item.uv); pvData.push(item.pv);
  });
  visitChart = echarts.init(visitChartDiv.value);
  visitChart.setOption({
    tooltip: {trigger: 'axis'},
    legend: {data: ['UV(独立访客)', 'PV(访问量)'], top: '90%', left: 'center'},
    grid: {top: '30px', left: '20px', right: '20px', bottom: '40px', containLabel: true},
    xAxis: {type: 'category', data: xData, axisLabel: {color: '#2F4F4F'}, axisLine: {lineStyle: {color: '#2F4F4F'}}},
    yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {show: true, lineStyle: {color: 'rgba(10,10,10,0.1)', width: 1, type: 'solid'}}},
    series: [
      {name: 'UV(独立访客)', type: 'line', stack: 'Total', data: uvData},
      {name: 'PV(访问量)', type: 'line', stack: 'Total', data: pvData}
    ]
  });
};

const initBarChart = () => {
  let xData: any[] = [], yData: any[] = [];
  tdata.data.popularThings?.forEach((item: any) => {
    xData.push(item.title); yData.push(item.count);
  });
  barChart = echarts.init(barChartDiv.value);
  barChart.setOption({
    grid: {top: '40px', left: '40px', right: '40px', bottom: '40px'},
    title: {text: '热门商品排名', textStyle: {color: '#aaa', fontSize: 18}, x: 'center', y: 'top'},
    tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
    xAxis: {data: xData, type: 'category', axisLabel: {rotate: 30, color: '#2F4F4F'}, axisLine: {lineStyle: {color: '#2F4F4F'}}},
    yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {show: true, lineStyle: {color: 'rgba(10,10,10,0.1)', width: 1, type: 'solid'}}},
    series: [{data: yData, type: 'bar', itemStyle: {normal: {color: '#70B0EA'}}}]
  });
};

const initPieChart = () => {
  let pieData: any[] = [];
  tdata.data.popularClassification?.forEach((item: any) => {
    pieData.push({name: item.title, value: item.count});
  });
  pieChart = echarts.init(pieChartDiv.value);
  const colorList = ['#70B0EA', '#B3A3DA', '#88DEE2', '#62C4C8', '#58A3A1'];
  pieChart.setOption({
    grid: {top: '40px', left: '40px', right: '40px', bottom: '40px'},
    title: {text: '热门商品分类', textStyle: {color: '#aaa', fontSize: 18}, x: 'center', y: 'top'},
    tooltip: {trigger: 'item'},
    legend: {top: '90%', left: 'center'},
    series: [{
      name: '分类', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
      itemStyle: {normal: {color: (params: any) => colorList[params.dataIndex % colorList.length]}},
      label: {show: false, position: 'center'},
      emphasis: {label: {show: true, fontSize: 20, fontWeight: 'bold'}},
      labelLine: {show: false}, data: pieData
    }]
  });
};
</script>

<style lang="less" scoped>
.main {
  height: 100%;
  display: flex;
  gap: 16px;
  flex-direction: column;
  padding: 16px;
  background: #f0f2f5;

  .box {
    padding: 12px;
    display: flex;
    flex-direction: column;

    .box-top {
      display: flex;
      flex-direction: row;
      align-items: center;
    }

    .box-value {
      color: #000;
      font-size: 32px;
      margin-right: 12px;

      .v-e {
        font-size: 14px;
      }
    }

    .box-bottom {
      margin-top: 24px;
      color: #000000d9;
    }
  }

  .chart-card {
    background: #fff;
    border-radius: 4px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 8px 0;
    padding-left: 8px;
    border-left: 4px solid #1890ff;
  }
}
</style>
