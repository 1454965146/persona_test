Component({
  properties: {
    scores: { type: Object, value: {} },
    scoresB: { type: Object, value: null },
    labels: { type: Array, value: ["外向", "直觉", "思考", "判断", "开放"] },
    colorA: { type: String, value: "#a29bfe" },
    colorB: { type: String, value: "#fd79a8" },
    size: { type: Number, value: 320 }
  },

  data: { canvasReady: false },

  lifetimes: {
    attached() { this.initCanvas(); }
  },

  observers: {
    'scores, scoresB'() { if (this.data.canvasReady) this.draw(); }
  },

  methods: {
    initCanvas() {
      var self = this;
      var query = wx.createSelectorQuery().in(this);
      query.select('#radarCanvas')
        .fields({ node: true, size: true })
        .exec(function(res) {
          if (!res || !res[0] || !res[0].node) {
            self.setData({ canvasReady: false });
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
          self.setData({ canvasReady: true });
          self.draw();
        });
    },

    draw() {
      var ctx = this._ctx;
      var size = this._size;
      if (!ctx) return;

      var cx = size / 2, cy = size / 2, r = size * 0.35;
      var labels = this.data.labels;
      var n = labels.length;
      var scoresA = this.data.scores, scoresB = this.data.scoresB;
      var dims = ['EI','SN','TF','JP','EXTRA'];
      var isCompare = !!scoresB;

      ctx.clearRect(0, 0, size, size);

      // Concentric pentagons
      for (var level = 1; level <= 4; level++) {
        ctx.beginPath();
        for (var i = 0; i < n; i++) {
          var angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
          var lr = r * level / 4;
          var px = cx + lr * Math.cos(angle);
          var py = cy + lr * Math.sin(angle);
          if (i === 0) ctx.moveTo(px, py); else ctx.lineTo(px, py);
        }
        ctx.closePath();
        ctx.strokeStyle = 'rgba(255,255,255,0.08)';
        ctx.lineWidth = 1;
        ctx.stroke();
      }

      // Axis lines + labels
      for (var i = 0; i < n; i++) {
        var angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(cx + r * Math.cos(angle), cy + r * Math.sin(angle));
        ctx.strokeStyle = 'rgba(255,255,255,0.12)';
        ctx.lineWidth = 1;
        ctx.stroke();

        var lx = cx + (r + 24) * Math.cos(angle);
        var ly = cy + (r + 24) * Math.sin(angle);
        ctx.fillStyle = '#a8a8b8';
        ctx.font = '12px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(labels[i], lx, ly);
      }

      // Data polygon A
      this.drawPolygon(ctx, scoresA, dims, cx, cy, r, this.data.colorA, 0.25);

      // Data polygon B
      if (isCompare) {
        this.drawPolygon(ctx, scoresB, dims, cx, cy, r, this.data.colorB, 0.25);
      }

      // Data points A
      for (var i = 0; i < n; i++) {
        var angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
        var val = (scoresA[dims[i]] || 3) / 5;
        var px = cx + r * val * Math.cos(angle);
        var py = cy + r * val * Math.sin(angle);

        ctx.fillStyle = '#f0f0f5';
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText((scoresA[dims[i]] || 3).toFixed(1), px, py - 12);

        ctx.beginPath();
        ctx.arc(px, py, 4, 0, 2 * Math.PI);
        ctx.fillStyle = this.data.colorA;
        ctx.fill();
      }

      // Data points B
      if (isCompare) {
        for (var i = 0; i < n; i++) {
          var angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
          var val = (scoresB[dims[i]] || 3) / 5;
          var px = cx + r * val * Math.cos(angle);
          var py = cy + r * val * Math.sin(angle);
          ctx.beginPath();
          ctx.arc(px, py, 4, 0, 2 * Math.PI);
          ctx.fillStyle = this.data.colorB;
          ctx.fill();
        }
      }

      // Legend
      ctx.fillStyle = this.data.colorA;
      ctx.fillRect(cx - 60, cy + r + 40, 12, 12);
      ctx.fillStyle = '#a8a8b8';
      ctx.font = '12px sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText(isCompare ? '你' : '你的分数', cx - 44, cy + r + 50);

      if (isCompare) {
        ctx.fillStyle = this.data.colorB;
        ctx.fillRect(cx + 10, cy + r + 40, 12, 12);
        ctx.fillStyle = '#a8a8b8';
        ctx.font = '12px sans-serif';
        ctx.fillText('对方', cx + 26, cy + r + 50);
      }
    },

    drawPolygon(ctx, scores, dims, cx, cy, r, color, alpha) {
      var n = dims.length;
      ctx.beginPath();
      for (var i = 0; i < n; i++) {
        var angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
        var val = (scores[dims[i]] || 3) / 5;
        var px = cx + r * val * Math.cos(angle);
        var py = cy + r * val * Math.sin(angle);
        if (i === 0) ctx.moveTo(px, py); else ctx.lineTo(px, py);
      }
      ctx.closePath();
      ctx.globalAlpha = alpha;
      ctx.fillStyle = color;
      ctx.fill();
      ctx.globalAlpha = 1;
      ctx.strokeStyle = color;
      ctx.lineWidth = 2;
      ctx.stroke();
    }
  }
});
