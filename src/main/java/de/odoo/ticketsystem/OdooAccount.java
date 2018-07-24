package de.odoo.ticketsystem;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@RestController
public class OdooAccount {

    @Value("${odoo.url}")
    private String url;
    @Value("${odoo.db}")
    private String db ;
    @Value("${odoo.username}")
    private String username;
    @Value("${odoo.password}")
    private String password;
    private XmlRpcClient client;
    private XmlRpcClient models;
    private boolean loggedIn = false;
    private int uid;

    private static final Map<String, Integer> odooColors;
    static
    {
        odooColors = new HashMap<>();
        odooColors.put("white",	0);
        odooColors.put("red", 1);
        odooColors.put("orange", 2);
        odooColors.put("yellow", 3);
        odooColors.put("blue", 4);
    }

    @RequestMapping("")
    public String authenticate() throws MalformedURLException {
        client = new XmlRpcClient();
        final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        common_config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        try {

            uid = (int)client.execute(
                    common_config, "authenticate", asList(
                            db, username, password, emptyMap()));
            loggedIn = true;
            return "Login Successful!";
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping("create")
    public String createTicket() throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            int id = (Integer)models.execute("execute_kw", asList(
                    db, uid, password,
                    "helpdesk.ticket", "create",
                    asList(new HashMap() {{ put("name", "Test ticket"); }})
            ));
            return "" + id;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    @RequestMapping("search-read")
    public List readTicket(@RequestParam(value = "name", defaultValue = "Test ticket")String name)
            throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            return asList((Object[])models.execute("execute_kw", asList(
                    db, uid, password,
                    "helpdesk.ticket", "search_read",
                    asList(asList(
                            asList("name", "=", name))),
                    new HashMap() {{
                        put("fields", asList("name", "country_id", "im_status", "comment", "color"));
                    }}
            )));
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return asList(e.getMessage());
        }
    }
    @RequestMapping("getfields")
    public Map<String, Map<String, Object>> getFields() throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            return (Map<String, Map<String, Object>>)models.execute("execute_kw", asList(
                    db, uid, password,
                    "helpdesk.ticket", "fields_get",
                    emptyList(),
                    new HashMap() {{
                        put("attributes", asList("string", "help", "type"));
                    }}
            ));
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return null;
        }
    }
    @RequestMapping("update")
    public String updateRecord(@RequestParam Map<String,String> requestParams) throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            models.execute("execute_kw", asList(
                    db, uid, password,
                    "helpdesk.ticket", "write",
                    asList(
                            asList(Integer.parseInt(requestParams.get("id"))),
                            new HashMap() {{ put(requestParams.get("fieldName"),
                                    Integer.parseInt(requestParams.get("value"))); }}
                    )
            ));
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return null;
    }
}
