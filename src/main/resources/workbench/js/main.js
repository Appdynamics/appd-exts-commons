/**
 * Created by abey.tom on 3/16/16.
 */

function initTree() {
    var height = $(window).height() - (50 + 20 + 74);
    console.log('height '+height)
    $('#metric-tree').parent().css('height', height + 'px');
    $.ajax({
        url: "../api/metric-tree",
        dataType: 'json'
    }).done(function (data) {
        $('#metric-tree').treeview({
            data: data,
            onNodeSelected: nodeSelected,
            onNodeUnselected: nodeUnSelected
        });
    })
    scheduleTasks();
}

var lastRefreshed = new Date().getTime();

function scheduleTasks() {
    $.ajax({
        url: '../api/stats',
        dataType: 'json'
    }).done(function (data) {
        if (data.lastRefreshed > lastRefreshed) {
           $('#last-refreshed').css('display','inline-block');
        }
        $('#metric-count').text('Total Registered Metrics = ' + data.metricCount);
        showErrors(data);
    }).always(function () {
        setTimeout(scheduleTasks, 3000);
    })
}

var showErrors = function (data) {
    if (data.errors != undefined && data.errors.length > 0) {
        var html = '<ol>';
        for (var i = 0; i < data.errors.length; i++) {
            var err = data.errors[i];
            html += '<li><div><b>' + err.message + "</b></div>";
            html += '<div>' + err.stackTrace + "</div></li>";
        }
        html+= '</ol>'
        $('#error-details').html(html);
    } else {
        $('#error-details').html('None so far!');
    }
};

function nodeSelected(event, node) {
    if (node.metricPath != undefined) {
        $.ajax({
            url: '../api/metric-data',
            data: {"metric-path": node.metricPath},
            dataType: 'json'
        }).done(function (data) {
            chart.add(data);
        })
    }
}

function nodeUnSelected(event, node) {
    if (node.metricPath != undefined) {

    }
}

var chart = {
    add: function (metricData) {
        var parent = $('#chart-parent');
        parent.empty();
        var paths = metricData[0].metricPath.split("|");
        var data = [{
            color: "#1f77b4",
            key: paths[paths.length - 1],
            values: metricData[0].values
        }];
        nv.addGraph(function () {
            var chart = nv.models.lineChart()
                .useInteractiveGuideline(true)
                .x(function (d) {
                    return d.timestamp
                })
                .y(function (d) {
                    return d.value
                })
                .color(d3.scale.category20().range())
                .margin({top: 15, right: 30, bottom: 20, left: 60})


            chart.xAxis
                .tickFormat(function (d) {
                    return d3.time.format('%H:%M:%S')(new Date(d))
                });

            chart.yAxis.tickFormat(d3.format(".3s"));

            d3.select(parent[0]).append("svg")
                .attr('height', '400')
                .datum(data)
                .transition().duration(500)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    }
};

