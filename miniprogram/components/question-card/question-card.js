Component({
  properties: { question: Object, index: Number, selected: Number },
  data: {
    options: [
      { val: 1, label: "非常不同意" },
      { val: 2, label: "不同意" },
      { val: 3, label: "中立" },
      { val: 4, label: "同意" },
      { val: 5, label: "非常同意" }
    ]
  },
  methods: {
    onSelect: function(e) {
      this.triggerEvent("select", { value: e.currentTarget.dataset.val });
    }
  }
});