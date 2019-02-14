package com.pxqngu.workbook;

import cn.hutool.core.text.StrSpliter;
import cn.hutool.db.*;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "WorkBookDetailsServlet")
public class WorkBookDetailsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-type" , "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        //获取所有请求参数列表
        Map<String,String[]> requestParameterMap = request.getParameterMap();
        String oper = requestParameterMap.get("oper")[0];//操作
        String bookID = requestParameterMap.get("id")[0];

        String bookName = "";
        String responseStr = "";//返回的字符串。

        try {
            switch (oper) {
                case "insert":
                case "add":
                    break;
                case "delete":
                case "del":

                    break;
                case "update":
                case "edit":
                    Db.use().update(
                            Entity.create()
                                    .set("book_name" , request.getParameter("name"))
                                    .set("note" , request.getParameter("note")) ,
                            Entity.create("work_book")
                                    .set("book_id" , request.getParameter("id"))
                    );
                    break;
//                  抄表册详情查询
                case "query":
                    int rows = Integer.valueOf(requestParameterMap.get("rows")[0]);//每页多少行
                    int page = Integer.valueOf(requestParameterMap.get("page")[0]);//第几页
//                    String sidx = requestParameterMap.get("sidx")[0];//排序字段
                    String sord = requestParameterMap.get("sord")[0];//排序

                    //定义查询的字段并转换成list
                    String[] selectFieldArray = {"customers_id","customers_name","customers_address",
                            "customers_mobile_main","customers_mobile","now_num","order_num","yc"};
                    List<String>  selectFieldList = Arrays.asList(selectFieldArray);
                    //定义排序
                    Order order = new Order("(order_num+0)" , (sord.equals("asc"))? Direction.ASC:Direction.DESC);
                    //传递页码和每页条目数并设置排序
                    Page queryPage = new Page(page , rows);
                    queryPage.addOrder(order);
                    //查询
                    PageResult<Entity> result = Db.use().page(selectFieldList ,
                            Entity.create("customers").set("book_id" , bookID) , queryPage);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("page", result.getPage());//页码
                    jsonObject.put("total", result.getTotalPage());//总页数
                    jsonObject.put("records", result.getTotal());//总记录数.
                    JSONArray rowsNum = new JSONArray();
                    for (Entity entity : result) {
                        JSONObject cell = new JSONObject();
                        cell.put("id", entity.getStr("customers_id"));
                        cell.put("name", entity.getStr("customers_name"));
                        cell.put("address", entity.getStr("customers_address"));
                        cell.put("phone1", entity.getStr("customers_mobile_main"));
//                        cell.put("phone2", entity.getStr("customers_mobile"));
                        cell.put("nownum", entity.getStr("now_num"));
                        cell.put("yc", entity.getStr("yc"));
                        cell.put("ordernum", entity.getStr("order_num"));
                        rowsNum.add(cell);
                    }
                    jsonObject.put("rows", rowsNum);
                    System.out.println(jsonObject.toString());
                    responseStr = jsonObject.toString();
                    break;
                case "getBookName"://获取抄表册名
                    bookName = Db
                            .use()
                            .queryOne("select work_book.book_name from work_book where book_id=" + bookID)
                            .getStr("book_name");
                    responseStr = bookName;
                    break;
                case "queryId"://载入数据
                    List<Entity> idList = Db.use().query(
                            "SELECT customers_id,book_id,order_num " +
                                    "FROM customers where book_id=? ORDER BY (order_num + 0) asc",
                            bookID
                    );
//// 返回json数据
// [{"id":"1024271","book_id":"3","order_num":"2001"},{"id":"1024272","book_id":"3","order_num":"2002"},{"id":"1024273","book_id":"3","order_num":"2003"}]
                    JSONArray jsonArray = new JSONArray();
                    for (Entity entity: idList){
                        JSONObject idListObj = new JSONObject();
                        idListObj.put("id" , entity.getStr("customers_id"));
                        idListObj.put("book_id" , entity.getStr("book_id"));
                        idListObj.put("order_num" , entity.getStr("order_num"));
                        jsonArray.add(idListObj);
                    }
                    responseStr = jsonArray.toString();
                    break;
                    //更新序号
                case "updateWorkBook":
                    /**
                     * 更改所属的抄表册需要先检查更改的编号是否存在别的抄表册中。
                     */
                    JSONObject responseJsonObject = new JSONObject();
                    StringBuffer msg = new StringBuffer();
                    boolean flag = false;
                    List<String> updateIDList = StrSpliter.split(
                            request.getParameter("updateIDList") ,
                            "\n" , 0 , true , true);//需要更新的用户ID列表
                    if (updateIDList.size() > 0){
                        List<Entity> queryIDList = Db.use().query("SELECT customers_id,book_id from customers where customers_id IN (" +
                                updateIDList.toString().substring(1 , updateIDList.toString().length() - 1) +")");
                        for (Entity entity : queryIDList){
                            if (!(entity.getStr("book_id").equals("1")) && !(entity.getStr("book_id").equals(bookID))){
                                msg.append("用户编号为:" + entity.getStr("customers_id")
                                        + "的用户已加入别的抄表册。抄表册编号为:" + entity.getStr("book_id") + "    \n");
                                flag = true;
                            }
                        }
                        if (flag){
                            responseJsonObject.put("code" , 1);
                            responseJsonObject.put("msg" , msg);
                        }else {
                            //先把数据设置成公海数据
                            Db.use().update(
                                    Entity.create().set("book_id" , 1) ,
                                    Entity.create("customers").set("book_id" , bookID)
                            );
                            Session session = Session.create();
                            try {
                                session.beginTransaction();
                                for (int i = 0; i < updateIDList.size(); i++) {
                                    session.update(
                                            Entity.create().set("book_id", bookID).set("order_num", i + 1),
                                            Entity.create("customers").set("customers_id", updateIDList.get(i)));
                                }
                                responseJsonObject.put("code", 0);
                                responseJsonObject.put("msg", "更新成功!");
                                session.commit();
                            } catch (SQLException ex) {
                                responseJsonObject.put("code", 1);
                                responseJsonObject.put("msg", "更新失败(SQLException)!");
                                ex.printStackTrace();
                                session.quietRollback();
                            }

                        }
                    }else {
                        //先把数据设置成公海数据
                        Db.use().update(
                                Entity.create().set("book_id" , 1) ,
                                Entity.create("customers").set("book_id" , bookID)
                        );
                        responseJsonObject.put("code", 0);
                        responseJsonObject.put("msg", "已清空抄表册!");
                    }
                    responseStr = responseJsonObject.toString();
                    break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        response.getWriter().println(responseStr);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
