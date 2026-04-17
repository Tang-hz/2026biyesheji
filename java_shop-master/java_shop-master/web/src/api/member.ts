import { get } from '/@/utils/http/axios';

enum URL {
    calcPrice = '/api/member/calcPrice',
    info = '/api/member/info',
}

const calcPriceApi = async (params: any) => get<any>({ url: URL.calcPrice, params: params, data: {}, headers: {} });

export { calcPriceApi };
