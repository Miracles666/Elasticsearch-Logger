# Elasticsearch-Logger 将ES流量导入burpsite

## 功能概述 / Feature Overview

该工具可以将 Burp Suite 的流量导入到 Elasticsearch 中，支持白名单过滤和通配符匹配。

This tool imports Burp Suite traffic into Elasticsearch, supporting whitelist filtering and wildcard matching.

### 白名单过滤 / Whitelist Filtering

- 支持通配符匹配
- 例如：`test.com` 将匹配所有包含 `test.com` 的域名

- Supports wildcard matching
- For example: `test.com` will match all domains containing `test.com`

## 使用方法 / Usage Instructions

1. 设置白名单 / Set up the whitelist
2. 配置 Elasticsearch 地址和端口 / Configure Elasticsearch address and port
3. 点击"连接"按钮开始导入 / Click the "Connect" button to start importing

## 步骤详解 / Detailed Steps

1. **白名单设置 / Whitelist Setup**
   - 在指定字段中输入需要过滤的域名
   - Enter the domains you want to filter in the designated field

2. **Elasticsearch 配置 / Elasticsearch Configuration**
   - 输入 Elasticsearch 服务器地址
   - 指定端口号（默认通常为 9200）
   - Enter the Elasticsearch server address
   - Specify the port number (default is usually 9200)

3. **开始连接 / Start Connect**
   - 点击"连接"按钮，工具将自动开始导入符合白名单条件的流量
   - Click the "Connect" button, and the tool will automatically start importing traffic that meets the whitelist criteria

## 注意事项 / Notes

- 确保 Elasticsearch 服务器正在运行且可访问
- 定期检查 Elasticsearch 以确保数据正确导入

- Ensure that the Elasticsearch server is running and accessible
- Importing large amounts of data may take some time, please be patient
- Regularly check Elasticsearch to ensure data is being imported correctly
