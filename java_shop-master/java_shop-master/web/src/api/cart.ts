import { get, post } from '/@/utils/http/axios';

enum URL {
  add = '/api/cart/add',
  updateCount = '/api/cart/updateCount',
  remove = '/api/cart/remove',
  list = '/api/cart/list',
  clear = '/api/cart/clear',
  count = '/api/cart/count',
}

const addCartApi = async (data: any) =>
  post<any>({
    url: URL.add,
    params: {},
    data,
    headers: { 'Content-Type': 'multipart/form-data;charset=utf-8' },
  });

const updateCartCountApi = async (data: any) =>
  post<any>({
    url: URL.updateCount,
    params: {},
    data,
    headers: { 'Content-Type': 'multipart/form-data;charset=utf-8' },
  });

const removeCartApi = async (params: any) =>
  post<any>({ url: URL.remove, params, headers: {} });

const listCartApi = async (params: any) =>
  get<any>({ url: URL.list, params, data: {}, headers: {} });

const clearCartApi = async (params: any) =>
  post<any>({ url: URL.clear, params, headers: {} });

const cartCountApi = async (params: any) =>
  get<any>({ url: URL.count, params, data: {}, headers: {} });

export { addCartApi, updateCartCountApi, removeCartApi, listCartApi, clearCartApi, cartCountApi };
