var app = getApp();
var api = require("../../utils/api");

Page({
  data: { reports: [], loading: true },

  onLoad: function() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/index/index" });
      return;
    }
  },

  onShow: function() {
    if (app.isLoggedIn()) this.loadHistory();
  },

  loadHistory: function() {
    var self = this;
    self.setData({ loading: true });
    api.getHistory().then(function(reports) {
      self.setData({ reports: reports || [], loading: false });
    }).catch(function(e) {
      wx.showToast({ title: e.message, icon: "none" });
      self.setData({ loading: false });
    });
  },

  openReport: function(e) {
    wx.navigateTo({ url: "/pages/report/report?reportCode=" + e.currentTarget.dataset.code });
  },

  openCompare: function(e) {
    wx.navigateTo({ url: "/pages/compare/compare?comparisonId=" + e.currentTarget.dataset.id });
  },

  goTest: function() { wx.navigateTo({ url: "/pages/index/index" }); }
});
