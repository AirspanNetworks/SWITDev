/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


$(function () {

    var buyerData = {
        labels: ["January", "February", "March", "April", "May", "June"],
        datasets: [
            {
                fillColor: "rgba(172,194,132,0.4)",
                strokeColor: "#ACC26D",
                pointColor: "#fff",
                pointStrokeColor: "#9DB86D",
                data: [203, 156, 99, 251, 305, 247]
            },
            {
                fillColor: "rgba(250,15,14,0.4)",
                strokeColor: "blue",
                pointColor: "yellow",
                pointStrokeColor: "gray",
                data: [0, 800, 10, 500, 3, 999]
            }
        ]
    };
    var graph = document.getElementById('graph').getContext('2d');
    new Chart(graph).Line(buyerData);


});
