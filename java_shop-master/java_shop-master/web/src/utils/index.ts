// import { resolve } from 'path';
// const fs = require('fs');
//
// function pathResolve(dir: string) {
//   return resolve(process.cwd(), '.', dir);
// }
//
// export const getFolder = (path: any) => {
//   const components: Array<string> = [];
//   const files = fs.readdirSync(path);
//   files.forEach(function (item: string) {
//     const stat = fs.lstatSync(path + '/' + item);
//     if (stat.isDirectory() === true && item != 'components') {
//       components.push(path + '/' + item);
//       components.push(pathResolve(path + '/' + item));
//     }
//   });
//   return components;
// };

export function getFormatTime(dateTime, flag) {
  if (dateTime != null) {
    var date;
    // 判断是否为时间戳（纯数字字符串或数值）
    if (/^\d+$/.test(dateTime)) {
      // 时间戳格式（毫秒）
      date = new Date(parseInt(dateTime));
    } else if (typeof dateTime === 'string' && dateTime.includes('-')) {
      // ISO 或 Datetime 格式，如 "2026-04-08T17:41:35" 或 "2026-04-08 17:41:35"
      // 将 T 替换为空格，兼容两种格式
      date = new Date(dateTime.replace('T', ' '));
    } else {
      // 其他情况尝试直接解析
      date = new Date(dateTime);
    }
    // 获取年份
    var YY = date.getFullYear();
    // 获取月份
    var MM = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1);
    // 获取日期
    var DD = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate());
    if (flag) { // flag 为 true，显示时分秒格式
      // 获取小时
      var hh = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours());
      // 获取分
      var mm = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
      // 获取秒
      var ss = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
      // 返回时间格式：2020-11-09 13:14:52
      return YY + '-' + MM + '-' + DD + ' ' + hh + ':' + mm + ':' + ss;
    } else {
      // 返回时间格式：2020-11-09
      return YY + '-' + MM + '-' + DD;
    }
  } else {
    return "";
  }
}
