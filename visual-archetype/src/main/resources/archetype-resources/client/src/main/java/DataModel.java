package ${package};

import com.dukescript.api.canvas.GraphicsContext2D;
import ${package}.js.Elements;
import net.java.html.charts.Chart;
import net.java.html.charts.Color;
import net.java.html.charts.Config;
import net.java.html.charts.Segment;
import net.java.html.charts.Values;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import net.java.html.leaflet.LatLng;
import net.java.html.leaflet.Map;
import net.java.html.leaflet.MapOptions;
import net.java.html.leaflet.Polygon;
import net.java.html.leaflet.Popup;
import net.java.html.leaflet.PopupOptions;
import net.java.html.leaflet.TileLayer;
import net.java.html.leaflet.TileLayerOptions;
import net.java.html.leaflet.event.MouseEvent.Type;

/** The "UI" of the page. Observes clicks on the top margin buttons and
 * switches the views according to active one.
 */
@Model(className = "Data", targetId="", instance = true, properties = {
    @Property(name = "active", type = String.class)
})
final class DataModel {
    private Elements.Listener onClick;
    private Chart<Values, Config> lineChart;
    private Chart<Segment, Config> pieChart;
    private Map map;

    @Function @ModelOperation
    void drawCanvas(Data model) {
        model.setActive("canvas");

        GraphicsContext2D ctx = GraphicsContext2D.getOrCreate("canvas");
        ctx.clearRect(0, 0, ctx.getWidth(), ctx.getHeight());
        ctx.setFillStyle(ctx.getWebColor("blue"));
        ctx.setStrokeStyle(ctx.getWebColor("#00007f"));
        ctx.setLineJoin("round");
        ctx.setLineWidth(5);
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        for (int i = 30; i < width - 30; i += 50) {
            ctx.beginPath();
            ctx.moveTo(i, 0);
            final int end = height * i / width;
            ctx.lineTo(i, end);
            ctx.stroke();
            ctx.fillRect(i - 10, end, 20, 20);
        }

        ctx.setFont("30px Monospaced");
        ctx.setFillStyle(ctx.getWebColor("black"));
        int[] size = Elements.screenSize();
        ctx.fillText("Hello from DukeScript!" + size[0] + " x " + size[1], 10, height - 30);

        if (onClick == null) {
            onClick = (ev) -> {
                ctx.setStrokeStyle(ctx.getWebColor("red"));
                ctx.setFillStyle(ctx.getWebColor("green"));
                ctx.beginPath();
                ctx.arc(ev.getX(), ev.getY() - 25, 50, 0, Math.PI * 2, false);
                ctx.stroke();
            };
            Elements.onClick("canvas", onClick);
        }
    }


    @Function @ModelOperation
    void lineChart(Data model) {
        model.setActive("lineChart");

        if (lineChart == null) {
            lineChart = Chart.createLine(
                new Values.Set("1st", Color.valueOf("gray"), Color.valueOf("black")),
                new Values.Set("2nd", Color.valueOf("lightgray"), Color.valueOf("yellow"))
            );
            lineChart.applyTo("lineChart");
        }

        lineChart.getData().add(new Values("#1", 3.0, 5.0));
        lineChart.getData().add(new Values("#2", 4.0, 6.0));
        lineChart.getData().add(new Values("#3", 6.0, 2.0));
        lineChart.getData().add(new Values("#4", 3.0, 1.0));
    }

    @Function @ModelOperation
    void pieChart(Data model) {
        model.setActive("pieChart");

        if (pieChart == null) {
            pieChart = Chart.createPie();
            pieChart.applyTo("pieChart");
        }

        final Color hoverColor = Color.valueOf("gray");
        pieChart.getData().add(new Segment("#1", 3.0, Color.valueOf("red"), hoverColor));
        pieChart.getData().add(new Segment("#2", 2.0, Color.valueOf("blue"), hoverColor));
        pieChart.getData().add(new Segment("#3", 1.0, Color.valueOf("green"), hoverColor));
        pieChart.getData().add(new Segment("#5", 4.0, Color.valueOf("yellow"), hoverColor));
    }

    @Function @ModelOperation
    void map(Data model) {
        model.setActive("map");

        final LatLng center = new LatLng(48.1322836,11.536);
        if (map != null) {
            map.setView(center);
            map.clearAllEventListeners();
        } else {
            MapOptions mapOptions = new MapOptions()
                    .setCenter(center)
                    .setZoom(17);
            map = new Map("map", mapOptions);

            // add a tile layer to the map
            TileLayerOptions tlo = new TileLayerOptions();
            tlo.setAttribution("Map data &copy; <a href='https://www.thunderforest.com/opencyclemap/'>OpenCycleMap</a> contributors, "
                    + "<a href='https://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
                    + "Imagery © <a href='https://www.thunderforest.com/'>Thunderforest</a>");
            tlo.setMaxZoom(18);
            TileLayer layer = new TileLayer("https://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png", tlo);
            map.addLayer(layer);
        }

        Polygon polygonLayer = new Polygon(new LatLng[] {
            new LatLng(48.13159, 11.53622),
            new LatLng(48.1325, 11.5370),
            new LatLng(48.1335, 11.5370),
            new LatLng(48.1321, 11.5350),
        });
        polygonLayer.addMouseListener(Type.CLICK, (ev) -> {
            PopupOptions popupOptions = new PopupOptions().setMaxWidth(400);
            Popup popup = new Popup(popupOptions);
            popup.setLatLng(ev.getLatLng());
            popup.setContent("DukeScript brand got invented here!");
            popup.openOn(map);
        });
        map.addLayer(polygonLayer);

        map.addMouseListener(Type.CLICK, (ev) -> {
            final LatLng at = ev.getLatLng();
            PopupOptions popupOptions = new PopupOptions().setMaxWidth(400);
            Popup popup = new Popup(popupOptions);
            popup.setLatLng(at);
            popup.setContent("@" + at.getLatitude() + "," + at.getLongitude());
            popup.openOn(map);
        });
    }

    private static Data ui;
    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
        ui.applyBindings();
    }
}
