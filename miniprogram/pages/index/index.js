var app = getApp();
var api = require("../../utils/api");

Page({
  data: {
    nickname: '',
    inviteCode: '',
    isLoggedIn: false,
    inviteInfo: null,
    inviteError: '',
    showLogin: false,
    loginMode: 'login',
    loginAccount: '',
    loginPassword: '',
    loginNickname: '',
    loginError: '',
    pendingAction: null,
    devLoginEnabled: true,
    wechatMockEnabled: false
  },

  onLoad: function(options) {
    this.syncLoginState();
    this.loadPublicConfig();
    var sc = options.shareCode || app.globalData.shareCode || '';
    if (sc) {
      this.setData({ inviteCode: sc });
      this.verifyInviteCode(sc);
    }
  },

  syncLoginState: function() {
    this.setData({
      isLoggedIn: app.isLoggedIn(),
      nickname: app.globalData.nickname || ''
    });
  },

  loadPublicConfig: function() {
    var self = this;
    api.getPublicConfig().then(function(config) {
      self.setData({
        devLoginEnabled: !!config.devLoginEnabled,
        wechatMockEnabled: !!config.wechatMockEnabled
      });
    }).catch(function() {});
  },

  onInviteCodeInput: function(e) {
    var code = e.detail.value.trim().toUpperCase();
    this.setData({ inviteCode: code, inviteInfo: null, inviteError: '' });
    if (code.length >= 4) this.verifyInviteCode(code);
  },
  onAccountInput: function(e) { this.setData({ loginAccount: e.detail.value, loginError: '' }); },
  onPasswordInput: function(e) { this.setData({ loginPassword: e.detail.value, loginError: '' }); },
  onLoginNicknameInput: function(e) { this.setData({ loginNickname: e.detail.value, loginError: '' }); },

  preventBubble: function() {},

  verifyInviteCode: function(code) {
    var self = this;
    api.getShareInfo(code).then(function(info) {
      app.globalData.shareCode = code;
      self.setData({ inviteInfo: info, inviteError: '' });
    }).catch(function(e) {
      self.setData({ inviteInfo: null, inviteError: '邀请码无效或已过期' });
    });
  },

  guardAction: function(actionName) {
    if (this.data.isLoggedIn) {
      this[actionName]();
    } else {
      this.openLogin('login', actionName);
    }
  },

  openLogin: function(mode, pendingAction) {
    this.setData({
      showLogin: true,
      loginMode: mode || 'login',
      loginError: '',
      loginAccount: '',
      loginPassword: '',
      loginNickname: '',
      pendingAction: pendingAction || null
    });
  },
  showLoginModal: function() { this.openLogin('login', null); },
  hideLoginModal: function() { this.setData({ showLogin: false, loginError: '' }); },
  toggleLoginMode: function() {
    this.setData({ loginMode: this.data.loginMode === 'login' ? 'register' : 'login', loginError: '' });
  },

  doWechatLogin: function() {
    var self = this;
    wx.login({
      success: function(res) {
        if (!res.code) {
          self.setData({ loginError: '微信登录失败，请重试' });
          return;
        }
        wx.showLoading({ title: '登录中...' });
        api.wechatLogin(res.code, self.data.loginNickname).then(function(data) {
          wx.hideLoading();
          self.finishLogin(data);
        }).catch(function(e) {
          wx.hideLoading();
          self.setData({ loginError: e.message });
        });
      },
      fail: function() {
        self.setData({ loginError: '微信登录失败，请检查网络' });
      }
    });
  },

  doLogin: function() {
    var self = this;
    var account = this.data.loginAccount.trim();
    var password = this.data.loginPassword.trim();
    if (!account || account.length < 2) { this.setData({ loginError: '账号至少2个字符' }); return; }
    if (!password) { this.setData({ loginError: '请输入密码' }); return; }

    wx.showLoading({ title: '登录中...' });
    if (this.data.loginMode === 'register') {
      api.devRegister(account, password, this.data.loginNickname || account).then(function(data) {
        wx.hideLoading();
        self.finishLogin(data);
      }).catch(function(e) { wx.hideLoading(); self.setData({ loginError: e.message }); });
    } else {
      api.devLogin(account, password).then(function(data) {
        wx.hideLoading();
        self.finishLogin(data);
      }).catch(function(e) { wx.hideLoading(); self.setData({ loginError: e.message }); });
    }
  },

  finishLogin: function(data) {
    app.saveAuth(data);
    var action = this.data.pendingAction;
    this.setData({
      isLoggedIn: true,
      nickname: data.user.nickname,
      showLogin: false,
      loginAccount: '',
      loginPassword: '',
      loginNickname: '',
      loginError: '',
      pendingAction: null
    });
    wx.showToast({ title: '登录成功', icon: 'success' });
    if (action) {
      var self = this;
      setTimeout(function() { self[action](); }, 600);
    }
  },

  startTest: function() {
    this.guardAction('doStartTest');
  },
  doStartTest: function() {
    var sc = this.data.inviteCode.trim().toUpperCase();
    if (sc && this.data.inviteInfo) app.globalData.shareCode = sc;
    wx.navigateTo({ url: '/pages/test/test' });
  },

  openHistory: function() {
    this.guardAction('doOpenHistory');
  },
  doOpenHistory: function() {
    wx.navigateTo({ url: '/pages/history/history' });
  },

  doLogout: function() {
    if (!this.data.isLoggedIn) {
      this.showLoginModal();
      return;
    }
    var self = this;
    wx.showModal({
      title: '退出登录',
      content: '退出后可以切换其他账号登录',
      success: function(res) {
        if (!res.confirm) return;
        api.logout().catch(function() {});
        app.clearAuth();
        self.setData({
          isLoggedIn: false,
          nickname: '',
          inviteInfo: null,
          inviteError: ''
        });
        wx.showToast({ title: '已退出', icon: 'success' });
      }
    });
  }
});
