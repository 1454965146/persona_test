var app = getApp();
var api = require("../../utils/api");
Page({
  data:{loading:true,reportCode:"",nickname:"",personalityType:"",dimList:[],scoresObj:{},reportHtml:"",showShare:false,selectedRel:"FRIEND",allowInviteeView:false,shareResult:null,relTypes:[{value:"FRIEND",label:"朋友"},{value:"COUPLE",label:"情侣"},{value:"BROTHER",label:"兄弟"},{value:"COLLEAGUE",label:"同事"},{value:"FAMILY",label:"亲子"}]},
  onLoad:function(o){if(!app.isLoggedIn()){wx.reLaunch({url:"/pages/index/index"});return}var rc=o.reportCode||app.globalData.reportCode;if(!rc){wx.showToast({title:"报告ID缺失",icon:"none"});return}this.setData({reportCode:rc});this.loadReport(rc)},
  loadReport:function(rc){var s=this;api.getReport(rc).then(function(r){var sc=r.dimensionScores||{};var dk={EI:"外向/内向",SN:"感觉/直觉",TF:"思考/情感",JP:"判断/感知",EXTRA:"开放度"};var dl=[];for(var k in dk){dl.push({key:dk[k],val:(sc[k]||3.0).toFixed(1),pct:Math.round(((sc[k]||3.0)/5)*100)})}var h=(r.content||"");h=h.replace(/### (.*)/g,'<h3 style="color:#c9bdff;margin:32rpx 0 16rpx;font-size:32rpx;">$1</h3>');h=h.replace(/## (.*)/g,'<h2 style="color:#f5f5f7;margin:40rpx 0 20rpx;font-size:36rpx;">$1</h2>');h=h.replace(/> (.*)/g,'<blockquote style="color:#a8a8b8;border-left:4rpx solid #6c5ce7;padding-left:24rpx;margin:16rpx 0;">$1</blockquote>');h=h.replace(/\*\*(.*?)\*\*/g,"<b>$1</b>");h=h.replace(/\n\n/g,"<br/><br/>");s.setData({loading:false,nickname:r.nickname,scoresObj:sc,personalityType:r.personalityType,dimList:dl,reportHtml:h})}).catch(function(e){wx.showToast({title:e.message,icon:"none"});s.setData({loading:false})})},
  showSharePicker:function(){this.setData({showShare:true})},hideSharePicker:function(){this.setData({showShare:false})},hideShareResult:function(){this.setData({shareResult:null})},selectRel:function(e){this.setData({selectedRel:e.currentTarget.dataset.value})},
  toggleInviteeView:function(e){this.setData({allowInviteeView:e.detail.value})},
  createShare:function(){var s=this;wx.showLoading({title:"生成中..."});api.createShare(this.data.reportCode,this.data.selectedRel,this.data.allowInviteeView).then(function(r){wx.hideLoading();s.setData({showShare:false,shareResult:r})}).catch(function(e){wx.hideLoading();wx.showToast({title:e.message,icon:"none"})})},
  onShareAppMessage:function(){var sc=this.data.shareResult?this.data.shareResult.shareCode:"";return{title:"来测测我们的性格本色吧！",path:"/pages/index/index?shareCode="+sc}},

copyCode:function(){var s=this.data.shareResult;if(!s){return}wx.setClipboardData({data:s.shareCode,success:function(){wx.showToast({title:"邀请码已复制",icon:"success"})}})},noop:function(){}
});
