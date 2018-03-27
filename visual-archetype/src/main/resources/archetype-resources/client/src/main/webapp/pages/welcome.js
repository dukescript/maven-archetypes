
(function() {

function info() {
    if (!document.head || !document.body) {
        throw 'Not ready yet';
    }

    var font = document.createElement("link");
    font.setAttribute("rel", "stylesheet");
    font.setAttribute("href", "https://use.fontawesome.com/releases/v5.0.8/css/all.css");
    font.setAttribute("integrity", "sha384-3AB7yXWz4OeoZcPbieVW64vVXEwADiYyAEhwilzWsLw+9FgqpyjjStpPnpBO8o8S");
    font.setAttribute("crossorigin", "anonymous");
    document.head.appendChild(font);


    var style = document.createElement("style");
    style.innerText =
    "            #footer{\n" +
    "                font-family: sans-serif;\n" +
    "                position: fixed;\n" +
    "                left: 0;\n" +
    "                bottom: 0;\n" +
    "                width: 100%;\n" +
    "                background-color: #132226;\n" +
    "                color: #132226;\n" +
    "                text-align: center;\n" +
    "            }\n" +
    "            .link-text {\n" +
    "                color: white;\n" +
    "            }\n" +
    "            #button-bar{\n" +
    "                padding: 1em;\n" +
    "                display: flex;\n" +
    "                justify-content: space-around;\n" +
    "            }\n" +
    "            .link-button{\n" +
    "                text-decoration: none; \n" +
    "                color: inherit;\n" +
    "                display:flex;\n" +
    "                flex-direction: column;\n" +
    "            }\n" +
    "            .icon-background {\n" +
    "                color: #dfe8eb;\n" +
    "            }\n" +
    "";
    document.head.appendChild(style);

    var div = document.createElement("div");
    div.id = "footer";
    div.innerHTML =
    "            <div id=\"button-bar\">\n" +
    "                <a class=\"link-button\" href=\"https://dukescript.com/blog.html\" target=\"_blank\" >\n" +
    "                    <span class=\"fa-stack fa-2x\">\n" +
    "                        <i class=\"fa fa-circle fa-stack-2x icon-background\"></i>\n" +
    "                        <i class=\"fa fa-rss-square fa-stack-1x\"></i>\n" +
    "                    </span>\n" +
    "                    <span class=\"link-text\">Blog</span>\n" +
    "                </a>\n" +
    "                <a class=\"link-button\" href=\"https://leanpub.com/dukescript\" target=\"_blank\">\n" +
    "                    <span class=\"fa-stack fa-2x\">\n" +
    "                        <i class=\"fa fa-circle fa-stack-2x icon-background\" ></i>\n" +
    "                        <i class=\"fa fa-book fa-stack-1x\"></i>\n" +
    "                    </span>\n" +
    "                    <span class=\"link-text\">Book</span>\n" +
    "                </a>\n" +
    "                <a class=\"link-button\" href=\"https://dukescript.com/documentation.html\" target=\"_blank\">\n" +
    "                    <span class=\"fa-stack fa-2x\">\n" +
    "                        <i class=\"fa fa-circle fa-stack-2x icon-background\"></i>\n" +
    "                        <i class=\"fa fa-file fa-stack-1x\"></i>\n" +
    "                    </span>\n" +
    "                    <span class=\"link-text\">Docs</span>\n" +
    "                </a>\n" +
    "                <a class=\"link-button\" href=\"https://dukescript.com/getting_started.html\" target=\"_blank\">\n" +
    "                    <span class=\"fa-stack fa-2x\">\n" +
    "                        <i class=\"fa fa-circle fa-stack-2x icon-background\"></i>\n" +
    "                        <i class=\"fa fa-university fa-stack-1x\"></i>\n" +
    "                    </span>\n" +
    "                    <span class=\"link-text\">Tutorials</span>\n" +
    "                </a>\n" +
    "            </div>\n" +
    "";

    document.body.appendChild(div);
}


var counter = 0;
function init() {
    if (counter++ > 3) {
        return;
    } else {
        try {
            info();
        } catch (e) {
            console.warn(e);
            console.log('Cannot initialize info section, visit: http://dukescript.com');
            window.setTimeout(init, 500);
        }
    }
}
window.onload = init;

})();