App({
  globalData: {
    apiBase: 'http://localhost:8080',
    sessionCode: '',
    reportCode: '',
    nickname: '',
    shareCode: '',
    token: '',
    user: null
  },

  onLaunch: function() {
    this.globalData.token = wx.getStorageSync('auth_token') || '';
    var user = null;
    try {
      user = wx.getStorageSync('auth_user') || null;
    } catch (e) {
      user = null;
    }
    this.globalData.user = user;
    this.globalData.nickname = user ? user.nickname : '';
  },

  saveAuth: function(data) {
    this.globalData.token = data.token;
    this.globalData.user = data.user;
    this.globalData.nickname = data.user ? data.user.nickname : '';
    wx.setStorageSync('auth_token', data.token);
    wx.setStorageSync('auth_user', data.user);
  },

  clearAuth: function() {
    this.globalData.token = '';
    this.globalData.user = null;
    this.globalData.nickname = '';
    wx.removeStorageSync('auth_token');
    wx.removeStorageSync('auth_user');
  },

  isLoggedIn: function() {
    return !!this.globalData.token && !!this.globalData.user;
  }
});
