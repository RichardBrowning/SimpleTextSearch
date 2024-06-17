package com.bradforj287.SimpleTextSearch;

import com.google.common.base.Stopwatch;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * Created by brad on 6/10/15.
 */
class main {

    public static void main(String args[]) throws Exception {
        // 获取存储Posts的文件
        File fXmlFile = new File("/Users/brad/Downloads/gaming.stackexchange.com/Posts.xml");
        // 创建Document Builder Factory，用于解析XML文件
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // 用Factory生产一个新Doc Builder
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // 用Doc Builder解析文件，生成w3c文件“doc”
        org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);

        // doc规范化处理，删除空白节点和父节点，合并相邻文本节点，
        doc.getDocumentElement().normalize();

        // 输出根节点
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        // 获取所有的row节点（所有名字是row的节点）
        NodeList nList = doc.getElementsByTagName("row");

        // 建立储存Document的列表
        List<Document> documentList = new ArrayList<>();
        // id向body映射：哈希表
        Map<String, String> idToBody = new HashMap<>();
        // 遍历所有的row节点
        for (int i = 0; i < nList.getLength(); i++) {
            // 获取本轮row节点
            Node n = nList.item(i);
            // 获取Body和Id属性
            String body = n.getAttributes().getNamedItem("Body").toString();
            String id = n.getAttributes().getNamedItem("Id").toString();
            // 创建Document对象
            Document document = new Document(body, id);
            // 将Document对象加入列表
            documentList.add(document);
            // 把当前id和body映射加入哈希表
            idToBody.put(id, body);
        }
        // 创建一个计时器
        Stopwatch sw = Stopwatch.createUnstarted();
        // 始
        sw.start();
        // 创建一个文件索引
        TextSearchIndex index = SearchIndexFactory.buildIndex(documentList);
        // 终
        sw.stop();

        // 输出索引构建完成所需时间
        System.out.println("finished building index took " + sw.toString());
        // 文件数量
        System.out.println("num documents: " + index.numDocuments());
        // 词数量
        System.out.println("num terms: " + index.termCount());

        // 创建一个扫描器
        Scanner scanner = new Scanner(System.in);

        // 搜素词为空
        String searchTerm = "";
        // 如果EXIT则退出
        while (!searchTerm.equalsIgnoreCase("EXIT")) {
            System.out.print("Enter your search terms or type EXIT: ");
            // 获取搜索词
            searchTerm = scanner.nextLine();
            sw.reset();
            sw.start();
            SearchResultBatch batch = index.search(searchTerm, 3);
            sw.stop();
            // 输出搜索结果
            System.out.println("printing results for term: " + searchTerm);
            for (SearchResult result : batch.getSearchResults()) {
                System.out.println("----------\n\n");
                System.out.println("score = " + result.getRelevanceScore());
                System.out.println(idToBody.get(result.getUniqueIdentifier().toString()));
            }
            // 输出搜索时间
            System.out.println("finished searching took: " + sw.toString());
            System.out.println("num documents searched: " + batch.getStats().getDocumentsSearched());

        }

    }
}
