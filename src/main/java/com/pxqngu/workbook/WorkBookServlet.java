package com.pxqngu.workbook;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
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
import java.util.List;

@WebServlet(name = "WorkBookServlet")
public class WorkBookServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-type" , "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String oper = request.getParameter("oper");
        String responseStr = "";

        try {
            switch (oper) {
                case "insert":
                case "add":
                    Db.use().insert(
                            Entity.create("work_book")
                                    .set("book_name", request.getParameter("bookName"))
                                    .set("new_date", DateUtil.now())
                    );
                    break;
                case "delete":
                case "del":
                    Db.use().del(
                            Entity.create("work_book")
                                    .set("book_id", request.getParameter("id"))
                    );
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
                case "query":
                    //查询抄表册信息
                    List<Entity> list = Db.use().query("SELECT * FROM `work_book`" );
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("page", 1);
                    jsonObject.put("total", 1);
                    jsonObject.put("records", list.size());
                    JSONArray rows = new JSONArray();
                    for (Entity entity : list) {
                        JSONObject cell = new JSONObject();
                        cell.put("id", entity.getStr("book_id"));
                        cell.put("name", entity.getStr("book_name"));
                        cell.put("sdate", entity.getStr("new_date"));
                        cell.put("note", entity.getStr("note"));
                        rows.add(cell);
                    }
                    jsonObject.put("rows", rows);
                    responseStr = jsonObject.toString();
                    break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        response.getWriter().write(responseStr);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
