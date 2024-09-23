package burp;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class BurpExtender implements IBurpExtender, IHttpListener, ITab {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private JPanel panel;
    private JTextField esHostField;
    private JTextField esPortField;
    private JTextField esIndexField;

    private JTextField whitelistField;

    private RestHighLevelClient esClient;
    private PrintWriter stdout;
    private Set<String> processedRequests = new HashSet<>();

    private Set<String> whitelistDomains = new HashSet<>();




    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        try {
            this.callbacks = callbacks;
            this.helpers = callbacks.getHelpers();
            this.stdout = new PrintWriter(callbacks.getStdout(), true);

            stdout.println("Starting to register extension...");

            callbacks.setExtensionName("Elasticsearch Logger");
            callbacks.registerHttpListener(this);

            createUI();

            callbacks.addSuiteTab(this);

        } catch (Exception e) {
            if (stdout != null) {
                stdout.println("Error in registerExtenderCallbacks: " + e.getMessage());
                e.printStackTrace(stdout);
            } else {
                e.printStackTrace();
            }
        }
    }

    private void createUI() {
        stdout.println("Entering createUI method...");

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 白名单输入框
        whitelistField = new JTextField(20);
        addLabelAndField("白名单地址（用|分割）:", whitelistField, gbc, 0);

        // Elasticsearch 配置输入框
        esHostField = new JTextField("192.168.31.78", 20);
        esPortField = new JTextField("9200", 20);
        esIndexField = new JTextField("burp_requests", 20);

        addLabelAndField("Elasticsearch Host:", esHostField, gbc, 1);
        addLabelAndField("Elasticsearch Port:", esPortField, gbc, 2);
        addLabelAndField("Elasticsearch Index:", esIndexField, gbc, 3);

        // 连接按钮
        JButton connectButton = new JButton("Connect to Elasticsearch");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(connectButton, gbc);

        connectButton.addActionListener(e -> {
            connectToElasticsearch();
            updateWhitelist();
        });

        stdout.println("UI creation completed.");
    }

    private void addLabelAndField(String labelText, JTextField field, GridBagConstraints gbc, int gridy) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }




    private void updateWhitelist() {
        String whitelist = whitelistField.getText();
        whitelistDomains.clear();
        for (String domain : whitelist.split("\\|")) {
            whitelistDomains.add(domain.trim().toLowerCase());
        }
        stdout.println("Updated whitelist: " + whitelistDomains);
    }

    public static List<String> getSuffix() {
        List<String> suffix = new ArrayList<>();
        suffix.add(".js");
        suffix.add(".css");
        suffix.add(".jpg");
        suffix.add(".png");
        suffix.add(".gif");
        suffix.add(".ico");
        suffix.add(".svg");
        suffix.add(".woff");
        suffix.add(".ttf");
        suffix.add(".eot");
        suffix.add(".woff2");
        suffix.add(".otf");
        suffix.add(".mp4");
        suffix.add(".mp3");
        suffix.add(".avi");
        suffix.add(".flv");
        suffix.add(".swf");
        suffix.add(".webp");
        suffix.add(".zip");
        suffix.add(".rar");
        suffix.add(".7z");
        suffix.add(".gz");
        suffix.add(".tar");
        suffix.add(".exe");
        suffix.add(".pdf");
        suffix.add(".doc");
        suffix.add(".docx");
        suffix.add(".xls");
        suffix.add(".xlsx");
        suffix.add(".ppt");
        suffix.add(".pptx");
        suffix.add(".txt");
        suffix.add(".xml");
        suffix.add(".apk");
        suffix.add(".ipa");
        suffix.add(".dmg");
        suffix.add(".iso");
        suffix.add(".img");
        suffix.add(".torrent");
        suffix.add(".jar");
        suffix.add(".war");
        suffix.add(".py");
        return suffix;
    }


    private void connectToElasticsearch() {
        stdout.println("Attempting to connect to Elasticsearch...");
        String host = esHostField.getText();
        int port;
        try {
            port = Integer.parseInt(esPortField.getText());
        } catch (NumberFormatException e) {
            stdout.println("Error: Invalid port number. Please enter a valid integer.");
            return;
        }

        stdout.println("Connecting to Elasticsearch at " + host + ":" + port);

        try {
            esClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, port, "http"))
            );
            stdout.println("RestHighLevelClient instance created.");

            // 尝试进行一个简单的操作来验证连接
            boolean isConnected = esClient.ping(RequestOptions.DEFAULT);
            if (isConnected) {
                stdout.println("Successfully connected to Elasticsearch. Cluster is responding.");
            } else {
                stdout.println("Warning: Elasticsearch cluster is not responding, but connection was established.");
            }
        } catch (IOException e) {
            stdout.println("IOException occurred while connecting to Elasticsearch: " + e.getMessage());
            e.printStackTrace(stdout);
        }
        catch (Exception e) {
            stdout.println("Unexpected exception occurred: " + e.getMessage());
            e.printStackTrace(stdout);
        }

        stdout.println("Elasticsearch connection attempt completed.");
    }

    private boolean isWhitelisted(String host) {
        for (String domain : whitelistDomains) {
            if (host.contains(domain)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (toolFlag == IBurpExtenderCallbacks.TOOL_PROXY && !messageIsRequest) {
            if (esClient == null) {
                stdout.println("Elasticsearch client is not connected. Please connect first.");
                return;
            }


            IRequestInfo requestInfo = helpers.analyzeRequest(messageInfo);


            String host = requestInfo.getUrl().getHost();
            if (!isWhitelisted(host)) {
                return;
            }

            URL url = requestInfo.getUrl();
            String urlStr = url.toString();

            // url 中为静态资源，直接返回
            List<String> suffix = getSuffix();
            String[] parts = urlStr.split("\\?");
            String suburl = urlStr;
            if (parts.length > 0) {
                suburl = parts[0];
            }
            for (String s : suffix) {
                if (suburl.endsWith(s)){
                    return;
                }
            }


            IResponseInfo responseInfo = helpers.analyzeResponse(messageInfo.getResponse());


            String method = requestInfo.getMethod();


            // Create a unique identifier for the request
            String requestId = method + url.toString() + new String(messageInfo.getRequest());

            // Check if this request has already been processed
            if (processedRequests.contains(requestId)) {
                return;
            }
            processedRequests.add(requestId);

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("url", url.toString());
            jsonMap.put("method", method);
            jsonMap.put("host", url.getHost());
            jsonMap.put("port", url.getPort());
            jsonMap.put("protocol", url.getProtocol());
            String req_headers = requestInfo.getHeaders().toString().replace("[","").replace("]","").replace(",","\\r\\n");
            jsonMap.put("request_headers", req_headers);
            jsonMap.put("request_body", new String(messageInfo.getRequest()).substring(requestInfo.getBodyOffset()));
            jsonMap.put("response_status", responseInfo.getStatusCode());
            String resp_headers = responseInfo.getHeaders().toString().replace("[","").replace("]","").replace(",","\\r\\n");
            jsonMap.put("response_headers", resp_headers);
            jsonMap.put("response_body", new String(messageInfo.getResponse()).substring(responseInfo.getBodyOffset()));
            jsonMap.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
            stdout.println(url);

            IndexRequest indexRequest = new IndexRequest(esIndexField.getText())
                    .type("document")
                    .source(jsonMap, XContentType.JSON);

            try {
                esClient.index(indexRequest, RequestOptions.DEFAULT);
                stdout.println("Indexed request: " + url);
            } catch (Exception e) {
                stdout.println("Failed to index request: " + e.getMessage());
            }
        }
    }

    @Override
    public String getTabCaption() {
        return "ES Logger";
    }

    @Override
    public Component getUiComponent() {
        if (panel == null) {
            stdout.println("Warning: UI panel is null in getUiComponent. This should not happen.");
            stdout.println("Creating a default panel as a fallback.");
            panel = new JPanel();
            panel.add(new JLabel("Error: UI was not properly initialized."));
        }
        return panel;
    }

}
