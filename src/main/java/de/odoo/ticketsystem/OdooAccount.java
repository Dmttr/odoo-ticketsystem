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
    public String createTicket(@RequestParam Map<String,String> requestParams) throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        String body = "<p style='margin:0px 0px 9px 0px; font-size:20px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'>Hey,</p><p style='margin:0px 0px 9px 0px; font-size:13px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'><br></p><p style='margin:0px 0px 9px 0px; font-size:13px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'>this is a test.</p><p style='margin:0px 0px 9px 0px; font-size:13px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'><br></p><p style='margin:0px 0px 9px 0px; font-size:13px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'>Regards</p><p style='margin:0px 0px 9px 0px; font-size:13px; font-family:\"Lucida Grande\", Helvetica, Verdana, Arial, sans-serif'>Username</p>";
        try {
            int id = (Integer)models.execute("execute_kw", asList(
                    db, uid, password,
                    "mail.compose.message", "create",
                    asList(new HashMap() {{
                        put("subject", requestParams.get("subject"));
                        put("res_id", Integer.parseInt(requestParams.get("res_id")));
                        put("model", "helpdesk.ticket");
                        put("body", body);
                        put("composition_mode", "comment");
                        put("is_log", true);
                    }})
            ));
            return "" + id;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    @RequestMapping("search-read")
    public List read(@RequestParam Map<String,String> requestParams)
            throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            return asList((Object[])models.execute("execute_kw", asList(
                    db, uid, password,
                    requestParams.get("entity"), "search_read",
                    asList(asList(
                            asList("subject", "=", requestParams.get("subject")))),
                    new HashMap() {{
                        put("fields", asList("email_from", "email_to", "subject", "attachment_ids", "body",
                                "model", "res_id", "composition_mode", "is_log"));
                    }}
            )));
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return asList(e.getMessage());
        }
    }
    @RequestMapping("search-readt")
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
                        put("fields", asList("name", "tag_ids"));
                    }}
            )));
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return asList(e.getMessage());
        }
    }

    @RequestMapping("getfields")
    public Map<String, Map<String, Object>> getFields(@RequestParam(value = "entity")String entityType)
            throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            return (Map<String, Map<String, Object>>)models.execute("execute_kw", asList(
                    db, uid, password,
                    entityType, "fields_get",
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
    public String updateRecord(@RequestParam Map<String,String> reqParams) throws MalformedURLException {
        models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        try {
            models.execute("execute_kw", asList(
                    db, uid, password,
                    reqParams.get("entity"), "write",
                    asList(
                            asList(Integer.parseInt(reqParams.get("id"))),
                            new HashMap() {{
                                put(reqParams.get("fieldName"),
                                    asList(asList(6, Integer.parseInt(reqParams.get("id")), asList(7,1)))
                                );
                            }}
                    )
            ));
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return null;
    }
}
