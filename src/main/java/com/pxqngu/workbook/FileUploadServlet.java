package com.pxqngu.workbook;

import cn.hutool.core.lang.Console;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import com.pxqngu.bean.ArrearsBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "FileUploadServlet")
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = -4187075130535308117L;
    private boolean isMultipart;
    private int maxFileSize = 1024 * 1024 * 100;
    private int maxMemSize = 100 * 1024;
    private List<ArrearsBean> customersList = null;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 检查是否有一个文件上传请求
        isMultipart = ServletFileUpload.isMultipartContent(request);
        String result = "";
        response.setContentType("text/html;charset=utf-8");
        if (!isMultipart) {
            result = "未获取到文件";
            response.getWriter().println(result);
            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 文件大小的最大值将被存储在内存中
        factory.setSizeThreshold(maxMemSize);
        // Location to save data that is larger than maxMemSize.
        String path = getServletContext().getRealPath("/") + "/";
        factory.setRepository(new File(path));
        // 创建一个新的文件上传处理程序
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 允许上传的文件大小的最大值
        upload.setSizeMax(maxFileSize);

        try {
            // 解析请求，获取文件项
            List fileItems = upload.parseRequest(request);
            // 处理上传的文件项
            Iterator i = fileItems.iterator();
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                if (!fi.isFormField()) {
                    // 获取上传文件的参数
                    String fieldName = fi.getFieldName();
                    String fileName = fi.getName();
                    String contentType = fi.getContentType();
                    boolean isInMemory = fi.isInMemory();
                    long sizeInBytes = fi.getSize();
                    // 写入文件
                    File file = new File(path +fileName);

                    if (fileName.contains("欠费")){
                        ExcelUtil.read03BySax(file , -1 , createRowHandler());
                        if (customersList != null){
                            Session session = Session.create();
                            try {
                                    Db.use().del(Entity.create("arrears").set("usercode" , "> 0"));
                                session.beginTransaction();
                                for (final ArrearsBean customers : customersList){
                                    Entity entity = Entity.create("arrears")
                                            .set("usercode" , customers.getUsercode())
                                            .set("username" , customers.getUsername())
                                            .set("useraddress" , customers.getUseraddress())
                                            .set("phone" , customers.getPhone())
                                            .set("date" , customers.getDate())
                                            .set("last_use_num" ,customers.getLast_use_num())
                                            .set("last_num" , customers.getLast_num())
                                            .set("now_num" , customers.getNow_num())
                                            .set("now_use_num" , customers.getNow_use_num())
                                            .set("new_last" , customers.getNew_last())
                                            .set("new_now" , customers.getNew_now())
                                            .set("arrears_num" , customers.getArrears_num())
                                            .set("yc" , customers.getYc());
                                    session.insert(entity);
                                }
                                session.commit();
                            }catch (SQLException ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            result = "上传成功";
        } catch (Exception ex) {
            result = "上传失败";
        }
        response.getWriter().println(result);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request , response);
    }

    /**
     * 对一行数据进行处理。
     * @return
     */
    private RowHandler createRowHandler() {
        customersList = new ArrayList<>();
        return new RowHandler() {
            @Override
            public void handle(int sheetIndex, int rowIndex, List<Object> rowlist) {
                if (rowIndex <= 7 || (rowlist.get(1).toString().trim().length() < 6)){
                    return;
                }
                ArrearsBean customers = new ArrearsBean();
                customers.setUsercode(rowlist.get(1).toString().trim());
                customers.setUsername(rowlist.get(2).toString().trim());
                customers.setUseraddress(rowlist.get(3).toString().trim());
                customers.setPhone(rowlist.get(4).toString().trim());
                customers.setDate(rowlist.get(5).toString().trim());
                customers.setLast_use_num(rowlist.get(6).toString().trim());
                customers.setLast_num(rowlist.get(7).toString().trim());
                customers.setNow_num(rowlist.get(8).toString().trim());
                customers.setNow_use_num(rowlist.get(9).toString().trim());
                customers.setNew_last(rowlist.get(10).toString().trim());
                customers.setNew_now(rowlist.get(11).toString().trim());
                customers.setArrears_num(rowlist.get(12).toString().trim());
                customers.setYc(rowlist.get(13).toString().trim());
                Console.log("[{}] [{}] {}", sheetIndex, rowIndex, rowlist);
                customersList.add(customers);
            }
        };
    }
}
