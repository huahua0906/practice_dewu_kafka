window.onload = function() {
    var canvas = document.getElementById('canvas');

    fetch("/sales").then(
        res=>res.json()
    ).then(res=>{
        var config = {
            type: 'line',
            data: {
                labels: res.dates,
                datasets: [{
                    label: '下单金额',
                    backgroundColor: '#3399FF',
                    borderColor: '#0000FF',
                    fill: false,
                    data: res.orderAmounts,
                }, {
                    label: '支付金额',
                    backgroundColor: '#FF6666',
                    borderColor: '#FF0000',
                    fill: false,
                    data: res.payAmounts,
                }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: '近 7 天销售业绩曲线图'
                },
                scales: {
                    xAxes: [{
                        display: true,
                    }],
                    yAxes: [{
                        display: true,
                        type: 'linear',
                    }]
                }
            }
        };

        var ctx = canvas.getContext('2d');
        window.myLine = new Chart(ctx, config);
    });

};