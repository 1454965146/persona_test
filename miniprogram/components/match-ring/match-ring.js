Component({
  properties: {
    percent: { type: Number, value: 0 },
    size: { type: Number, value: 240 },
    color: { type: String, value: "#a29bfe" },
    trackColor: { type: String, value: "rgba(255,255,255,0.08)" }
  },

  data: { ready: false },

  lifetimes: {
    attached() { this.initCanvas(); },
    detached() { this._ctx = null; }
  },

  observers: {
    'percent, size, color'() { if (this.data.ready) this.draw(); }
  },

  methods: {
    initCanvas() {
      var self = this;
      var query = wx.createSelectorQuery().in(this);
      query.select('#matchCanvas')
        .fields({ node: true, size: true })
        .exec(function(res) {
          if (!res || !res[0] || !res[0].node) {
            setTimeout(function() { self.initCanvas(); }, 100);
            return;
          }
          var canvas = res[0].node;
          var ctx = canvas.getContext('2d');
          var dpr = wx.getSystemInfoSync().pixelRatio;
          var size = self.data.size;
          canvas.width = size * dpr;
          canvas.height = size * dpr;
          ctx.scale(dpr, dpr);
          self._ctx = ctx;
          self._size = size;
          self.setData({ ready: true });
          self.draw();
        });
    },

    draw() {
      var ctx = this._ctx;
      var size = this._size;
      if (!ctx) return;

      var pct = Math.max(0, Math.min(100, this.data.percent));
      var cx = size / 2, cy = size / 2;
      var lineWidth = size * 0.08;
      var radius = size / 2 - lineWidth / 2;
      var startAngle = -Math.PI / 2;
      var endAngle = startAngle + (pct / 100) * Math.PI * 2;

      ctx.clearRect(0, 0, size, size);

      // 轨道
      ctx.beginPath();
      ctx.arc(cx, cy, radius, 0, Math.PI * 2);
      ctx.strokeStyle = this.data.trackColor;
      ctx.lineWidth = lineWidth;
      ctx.lineCap = 'round';
      ctx.stroke();

      // 进度弧
      if (pct > 0) {
        ctx.beginPath();
        ctx.arc(cx, cy, radius, startAngle, endAngle);
        ctx.strokeStyle = this.data.color;
        ctx.lineWidth = lineWidth;
        ctx.lineCap = 'round';
        ctx.stroke();
      }
    }
  }
});
