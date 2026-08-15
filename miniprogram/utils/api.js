var app = getApp();

function request(method, path, data) {
  return new Promise(function(resolve, reject) {
    var app = getApp();
    var token = app.globalData.token || wx.getStorageSync('auth_token') || '';
    var header = { "Content-Type": "application/json" };
    if (token) header["Authorization"] = "Bearer " + token;

    wx.request({
      url: app.globalData.apiBase + path,
      method: method,
      data: data,
      header: header,
      success: function(res) {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data.code === 200) {
          resolve(res.data.data);
        } else if (res.statusCode === 401) {
          app.clearAuth();
          wx.showToast({ title: "请重新登录", icon: "none" });
          setTimeout(function() {
            wx.reLaunch({ url: "/pages/index/index" });
          }, 500);
          reject(new Error("登录状态已失效"));
        } else {
          var msg = (res.data && res.data.message) ? res.data.message : "error";
          reject(new Error(msg));
        }
      },
      fail: function(err) {
        reject(new Error("network error"));
      }
    });
  });
}

module.exports = {
  request: request,
  getPublicConfig: function() { return request("GET", "/api/config/public"); },
  wechatLogin: function(code) { return request("POST", "/api/auth/wechat/login", { code: code }); },
  devLogin: function(username, password) { return request("POST", "/api/auth/dev/login", { username: username, password: password }); },
  devRegister: function(username, password, nickname) { return request("POST", "/api/auth/dev/register", { username: username, password: password, nickname: nickname }); },
  logout: function() { return request("POST", "/api/auth/logout"); },
  me: function() { return request("GET", "/api/me"); },
  getHistory: function() { return request("GET", "/api/me/history"); },
  getQuestions: function() { return request("GET", "/api/test/questions"); },
  submitAnswers: function(answers) { return request("POST", "/api/test/submit", { answers: answers }); },
  generateReport: function(sessionCode, nickname) { return request("POST", "/api/report/generate", { sessionCode: sessionCode, nickname: nickname }); },
  getReport: function(reportCode) { return request("GET", "/api/report/" + reportCode); },
  createShare: function(reportCode, relationshipType, allowInviteeView) {
    return request("POST", "/api/share/create", {
      reportCode: reportCode,
      relationshipType: relationshipType,
      allowInviteeView: !!allowInviteeView
    });
  },
  getShareInfo: function(shareCode) { return request("GET", "/api/share/" + shareCode); },
  bindShareInvitee: function(shareCode, reportCode) { return request("POST", "/api/share/" + shareCode + "/bind", { reportCode: reportCode }); },
  generateComparison: function(reportCodeA, reportCodeB, relationshipType) { return request("POST", "/api/compare/generate", { reportCodeA: reportCodeA, reportCodeB: reportCodeB, relationshipType: relationshipType }); },
  getLinksByReport: function(code) { return request("GET", "/api/share/by-report/" + code); },
  getComparison: function(comparisonId) { return request("GET", "/api/compare/" + comparisonId); },
  retryComparison: function(comparisonId) { return request("POST", "/api/compare/" + comparisonId + "/retry"); },
  createPaymentOrder: function(reportCode) { return request("POST", "/api/payment/order", { reportCode: reportCode }); },
  getPaymentOrder: function(orderNo) { return request("GET", "/api/payment/order/" + orderNo); },
  mockPaySuccess: function(orderNo) { return request("POST", "/api/payment/" + orderNo + "/mock-success"); }
};
