import {get, post} from '/@/utils/http/axios';

enum URL {
    list = '/api/overview/count',
    sysInfo = '/api/overview/sysInfo',
    userGrowth = '/api/overview/userGrowth',
    memberDistribution = '/api/overview/memberDistribution',
    salesTrend = '/api/overview/salesTrend',
    avgOrderValue = '/api/overview/avgOrderValue',
    orderStatus = '/api/overview/orderStatus',
    orderTrend = '/api/overview/orderTrend',
    thingScore = '/api/overview/thingScore',
    thingHot = '/api/overview/thingHot',
}

const listApi = async (params: any) =>
    get<any>({url: URL.list, params: params, data: {}, headers: {}});

const sysInfoApi = async (params: any) =>
    get<any>({url: URL.sysInfo, params: params, data: {}, headers: {}});

const userGrowthApi = async (params: any) =>
    get<any>({url: URL.userGrowth, params: params, data: {}, headers: {}});

const memberDistributionApi = async (params: any) =>
    get<any>({url: URL.memberDistribution, params: params, data: {}, headers: {}});

const salesTrendApi = async (params: any) =>
    get<any>({url: URL.salesTrend, params: params, data: {}, headers: {}});

const avgOrderValueApi = async (params: any) =>
    get<any>({url: URL.avgOrderValue, params: params, data: {}, headers: {}});

const orderStatusApi = async (params: any) =>
    get<any>({url: URL.orderStatus, params: params, data: {}, headers: {}});

const orderTrendApi = async (params: any) =>
    get<any>({url: URL.orderTrend, params: params, data: {}, headers: {}});

const thingScoreApi = async (params: any) =>
    get<any>({url: URL.thingScore, params: params, data: {}, headers: {}});

const thingHotApi = async (params: any) =>
    get<any>({url: URL.thingHot, params: params, data: {}, headers: {}});

export {
    listApi,
    sysInfoApi,
    userGrowthApi,
    memberDistributionApi,
    salesTrendApi,
    avgOrderValueApi,
    orderStatusApi,
    orderTrendApi,
    thingScoreApi,
    thingHotApi,
};
