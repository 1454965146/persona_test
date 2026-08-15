var app = getApp();
var api = require("../../utils/api");
Page({
  data:{shareCode:"",nickname:"",shareInfo:null,error:""},
  onLoad:function(o){if(!app.isLoggedIn()){wx.reLaunch({url:"/pages/index/index?shareCode="+(o.shareCode||"")});return}var sc=o.shareCode||app.globalData.shareCode;if(!sc){this.setData({error:"未找到邀请码，请通过朋友分享的链接进入"});return}this.setData({shareCode:sc});app.globalData.shareCode=sc;this.loadShareInfo(sc)},
  loadShareInfo:function(sc){var s=this;api.getShareInfo(sc).then(function(i){s.setData({shareInfo:i,error:""})}).catch(function(e){s.setData({error:e.message,shareInfo:null})})},
  onNicknameInput:function(e){this.setData({nickname:e.detail.value})},
  startTest:function(){if(!app.globalData.nickname){wx.showToast({title:"请先登录",icon:"none"});return}wx.navigateTo({url:"/pages/test/test"})}
});
