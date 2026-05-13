import {get, post} from '/@/utils/http/axios';

enum URL {
    create = '/api/pay/create',
    query = '/api/pay/query',
}

const createPayApi = async (data: any) =>
    post<any>({
        url: URL.create,
        params: {},
        data: data,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

const queryPayStatusApi = async (params: any) =>
    get<any>({url: URL.query, params: params, data: {}, headers: {}});

export {createPayApi, queryPayStatusApi};
