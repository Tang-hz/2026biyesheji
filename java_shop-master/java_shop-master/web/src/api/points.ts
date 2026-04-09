import { get, post } from '/@/utils/http/axios';

enum URL {
    getPoints = '/api/points/get',
    sign = '/api/points/sign',
    redeem = '/api/points/redeem',
    log = '/api/points/log',
    signed = '/api/points/signed'
}

const getPointsApi = async (params: any) => get<any>({ url: URL.getPoints, params: params, data: {}, headers: {} });
const signApi = async (params: any) => post<any>({ url: URL.sign, params: params, data: {}, headers: {} });
const redeemApi = async (params: any) => post<any>({ url: URL.redeem, params: params, data: {}, headers: {} });
const getPointsLogApi = async (params: any) => get<any>({ url: URL.log, params: params, data: {}, headers: {} });
const checkSignedApi = async (params: any) => get<any>({ url: URL.signed, params: params, data: {}, headers: {} });

export { getPointsApi, signApi, redeemApi, getPointsLogApi, checkSignedApi };
