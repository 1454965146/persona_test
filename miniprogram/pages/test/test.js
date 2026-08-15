var app = getApp();
var api = require("../../utils/api");

Page({
  data: {
    questions: [], currentIndex: 0, currentQuestion: null,
    answers: {}, percent: 0, submitting: false, loadError: false
  },
  onLoad: function() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/index/index" });
      return;
    }
    this.loadQuestions();
  },
  updatePercent: function() {
    var qs = this.data.questions;
    if (!qs.length) return;
    this.setData({ percent: Math.round(((this.data.currentIndex + 1) / qs.length) * 100) });
  },
  loadQuestions: function() {
    var self = this;
    wx.showLoading({ title: "加载题库..." });
    api.getQuestions().then(function(qs) {
      if (!qs || !qs.length) { self.setData({ loadError: true }); wx.hideLoading(); return; }
      self.setData({ questions: qs, currentQuestion: qs[0], loadError: false });
      self.updatePercent();
      wx.hideLoading();
    }).catch(function(e) {
      self.setData({ loadError: true });
      wx.hideLoading();
      wx.showToast({ title: "题库加载失败，请检查网络后重试", icon: "none", duration: 3000 });
    });
  },
  retryLoad: function() { this.loadQuestions(); },
  onAnswerSelect: function(e) {
    var val = e.detail.value;
    var answers = this.data.answers;
    answers[this.data.currentQuestion.id] = val;
    this.setData({ answers: answers });
  },
  prevQuestion: function() {
    if (this.data.currentIndex === 0) return;
    var ni = this.data.currentIndex - 1;
    this.setData({ currentIndex: ni, currentQuestion: this.data.questions[ni] });
    this.updatePercent();
  },
  nextQuestion: function() {
    var self = this, ci = this.data.currentIndex, qs = this.data.questions, cq = this.data.currentQuestion;
    if (!cq) { wx.showToast({ title: "题库未加载", icon: "none" }); return; }
    if (!this.data.answers[cq.id]) { wx.showToast({ title: "请选择答案", icon: "none" }); return; }
    if (ci === qs.length - 1) { self.submitTest(); return; }
    var ni = ci + 1;
    this.setData({ currentIndex: ni, currentQuestion: qs[ni] });
    this.updatePercent();
  },
  submitTest: function() {
    var self = this;
    self.setData({ submitting: true });
    api.submitAnswers(this.data.answers).then(function(r) {
      app.globalData.sessionCode = r.sessionCode;
      return api.generateReport(r.sessionCode, app.globalData.nickname);
    }).then(function(rr) {
      app.globalData.reportCode = rr.reportCode;
      // 被邀请者：仅绑定邀请码，后端会自动生成对比供邀请者查看。
      // 被邀请者答题完成后始终跳转个人报告页。
      if (app.globalData.shareCode) {
        api.bindShareInvitee(app.globalData.shareCode, rr.reportCode).then(function() {
          app.globalData.shareCode = '';
          wx.redirectTo({ url: "/pages/report/report?reportCode=" + rr.reportCode });
        }).catch(function() {
          wx.redirectTo({ url: "/pages/report/report?reportCode=" + rr.reportCode });
        });
      } else {
        wx.redirectTo({ url: "/pages/report/report?reportCode=" + rr.reportCode });
      }
    }).catch(function(e) {
      wx.showToast({ title: e.message, icon: "none", duration: 3000 });
      self.setData({ submitting: false });
    });
  }
});
