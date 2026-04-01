import {get, post} from '/@/utils/http/axios';

enum URL {
    list = '/api/notice/list',
    userList = '/api/notice/userList',
    create = '/api/notice/create',
    update = '/api/notice/update',
    delete = '/api/notice/delete',
    userDelete = '/api/notice/userDelete',
}

const listApi = async (params: any) =>
    get<any>({url: URL.list, params: params, data: {}, headers: {}});

const userListApi = async (params: any) =>
    get<any>({url: URL.userList, params: params, data: {}, headers: {}});
const createApi = async (data: any) =>
    post<any>({
        url: URL.create,
        params: {},
        data: data,
        headers: {'Content-Type': 'multipart/form-data;charset=utf-8'}
    });
const updateApi = async (data: any) =>
    post<any>({
        url: URL.update,
        data: data,
        headers: {'Content-Type': 'multipart/form-data;charset=utf-8'}
    });
const deleteApi = async (params: any) =>
    post<any>({url: URL.delete, params: params, headers: {}});

const userDeleteApi = async (params: any) =>
    post<any>({url: URL.userDelete, params: params, headers: {}});

export {listApi, userListApi, createApi, updateApi, deleteApi, userDeleteApi};
