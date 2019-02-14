jQuery(function ($) {
    var grid_selector = "#grid-table";
    var pager_selector = "#grid-pager";

    var parent_column = $(grid_selector).closest('[class*="col-"]');
    //调整大小以适应页面大小
    $(window).on('resize.jqGrid', function () {
        $(grid_selector).jqGrid('setGridWidth', parent_column.width());
    });

    //侧边栏折叠/展开时调整大小
    $(document).on('settings.ace.jqGrid', function (ev, event_name, collapsed) {
        if (event_name === 'sidebar_collapsed' || event_name === 'main_container_fixed') {
            //setTimeout仅用于WebKit，用于为DOM更改留出时间，然后重新绘制！！！！
            setTimeout(function () {
                $(grid_selector).jqGrid('setGridWidth', parent_column.width());
            }, 20);
        }
    })

    //jqGrid配置
    jQuery(grid_selector).jqGrid({
        // data: grid_data,
        url: 'workbookdetails.do?oper=query&id='+ getParam("id"),
        datatype: "json",
        height: "auto",
        colNames: ['排序号', '客户编号', '客户名称', '地址', '电话1', '当前吨位' , '预存'],
        colModel: [
            // {
            //     name: 'myac', index: '', width: 80, fixed: true, sortable: false, resize: false,
            //     formatter: 'actions',
            //     // formatter:function cLink(cellvalue, options, rowObject){
            //     //     return '<a href="javascript:void(0)" onclick=bootbox.alert("dfdfd")>查看详情</a>';
            //     // },
            //     formatoptions:{
            //         keys: true,
            //         delbutton: false,//disable delete button
            //         delOptions: {recreateForm: true, beforeShowForm: beforeDeleteCallback},
            //         //editformbutton:true, editOptions:{recreateForm: true, beforeShowForm:beforeEditCallback}
            //     }
            // },
            {name: 'ordernum', index: 'ordernum', width: 15, sorttype: "int", editable: false},
            {name: 'id', index: 'id', width: 25, sorttype: "int", editable: false},
            {
                name: 'name',
                index: 'name',
                width: 50,
                sortable: true,
                editable: true,
                edittype: "textarea",
                editoptions: {size: "20", maxlength: "30"},
                //formatter: "showlink",
                //formatoptions: {baseLinkUrl: "getBook.do", idName: "id"}
            },
            {name: 'address', index: 'address', width: 100, sorttype: "int", editable: false},
            // {name: 'phone2', index: 'phone2', width: 50, sorttype: "int", editable: false},
            {name: 'phone1', index: 'phone1', width: 50, sorttype: "int", editable: false},
            {name: 'nownum', index: 'nownum', width: 20, sorttype: "int", editable: false},
            {name: 'yc', index: 'yc', width: 20, sorttype: "int", editable: false},
            // {name: 'sdate', index: 'sdate', width: 90, editable: false, sortable:true ,sorttype: "date", unformat: pickDate},
            // {
            //     name: 'note',
            //     index: 'note',
            //     width: 150,
            //     sortable: true,
            //     editable: true,
            //     edittype: "textarea",
            //     editoptions: {rows: "2", cols: "10"}
            // }
        ],
        jsonReader: {
            repeatitems: false
        },
        //cellEdit:false,//是否允许编辑单元格
        viewrecords: true,
        rowNum: 500,
        rowList: [500, 1000, 2000],
        pager: pager_selector,
        altRows: true,
        //toppager: true,

        multiselect: true,//控制每行前面是否显示选择按钮框
        //multikey: "ctrlKey",
        multiboxonly: true,

        loadComplete: function () {
            var table = this;
            setTimeout(function () {
                styleCheckbox(table);
                updateActionIcons(table);
                updatePagerIcons(table);
                enableTooltips(table);
            }, 0);
        },

        //editurl: "workbook.do",//nothing is saved
        caption: "客户列表:",

        // autowidth: true,
        // grouping: true,
        // groupingView: {
        //     groupField: ['name'],
        //     groupDataSorted: true,
        //     plusicon: 'fa fa-chevron-down bigger-110',
        //     minusicon: 'fa fa-chevron-up bigger-110'
        // },
        // caption: "Grouping"
    });//----------------------------------------------------------------------------------jqGrid设置


    $(window).triggerHandler('resize.jqGrid');//trigger window resize to make the grid get the correct size

    //enable search/filter toolbar启用搜索/筛选工具栏
    // jQuery(grid_selector).jqGrid('filterToolbar',{defaultSearch:true,stringResult:true})
    // jQuery(grid_selector).filterToolbar({});

    //switch element when editing inline
    // 内联编辑时切换元素
    function aceSwitch(cellvalue, options, cell) {
        setTimeout(function () {
            $(cell).find('input[type=checkbox]')
                .addClass('ace ace-switch ace-switch-5')
                .after('<span class="lbl"></span>');
        }, 0);
    }

    //enable datepicker
    // 启用日期选取器
    function pickDate(cellvalue, options, cell) {
        setTimeout(function () {
            $(cell).find('input[type=text]')
                .datepicker({format: 'yyyy-mm-dd', autoclose: true});
        }, 0);
    }

    //----------------------------------------------------------------------------------底部导航
    //navButtons
    //导航按钮
    jQuery(grid_selector).jqGrid('navGrid', pager_selector,
        { 	//navbar options
            edit: true,
            editicon: 'ace-icon fa fa-pencil blue',
            add: false,
            addicon: 'ace-icon fa fa-plus-circle purple',
            del: false,
            delicon: 'ace-icon fa fa-trash-o red',
            search: true,
            searchicon: 'ace-icon fa fa-search orange',
            refresh: true,
            refreshicon: 'ace-icon fa fa-refresh green',
            view: false,
            viewicon: 'ace-icon fa fa-search-plus grey',
        },
        {
            //edit record form编辑记录窗体
            //closeAfterEdit: true,
            //width: 700,
            recreateForm: true,
            beforeShowForm: function (e) {
                var form = $(e[0]);
                form.closest('.ui-jqdialog').find('.ui-jqdialog-titlebar').wrapInner('<div class="widget-header" />');
                style_edit_form(form);
            }
        },
        {
            //new record form新建记录窗体
            //width: 700,
            closeAfterAdd: true,
            recreateForm: true,
            viewPagerButtons: true,
            beforeShowForm: function (e) {
                var form = $(e[0]);
                form.closest('.ui-jqdialog').find('.ui-jqdialog-titlebar')
                    .wrapInner('<div class="widget-header" />')
                style_edit_form(form);
            }
        },
        {
            //delete record form
            recreateForm: true,
            beforeShowForm: function (e) {
                var form = $(e[0]);
                if (form.data('styled')) return false;

                form.closest('.ui-jqdialog').find('.ui-jqdialog-titlebar').wrapInner('<div class="widget-header" />')
                style_delete_form(form);

                form.data('styled', true);
            },
            onClick: function (e) {
                //alert(1);
            }
        },
        {
            //search form
            recreateForm: true,
            afterShowSearch: function (e) {
                var form = $(e[0]);
                form.closest('.ui-jqdialog').find('.ui-jqdialog-title').wrap('<div class="widget-header" />')
                style_search_form(form);
            },
            afterRedraw: function () {
                style_search_filters($(this));
            }
            ,
            multipleSearch: true,
            /**
             multipleGroup:true,
             showQuery: true
             */
        },
        {
            //view record form
            recreateForm: true,
            beforeShowForm: function (e) {
                var form = $(e[0]);
                form.closest('.ui-jqdialog').find('.ui-jqdialog-title').wrap('<div class="widget-header" />')
            }
        }
    )

    function style_edit_form(form) {
        //enable datepicker on "sdate" field and switches for "stock" field
        form.find('input[name=sdate]').datepicker({format: 'yyyy-mm-dd', autoclose: true})

        form.find('input[name=stock]').addClass('ace ace-switch ace-switch-5').after('<span class="lbl"></span>');
        //don't wrap inside a label element, the checkbox value won't be submitted (POST'ed)
        //.addClass('ace ace-switch ace-switch-5').wrap('<label class="inline" />').after('<span class="lbl"></span>');

        //update buttons classes更新按钮类
        var buttons = form.next().find('.EditButton .fm-button');
        buttons.addClass('btn btn-sm').find('[class*="-icon"]').hide();//ui-icon, s-icon
        buttons.eq(0).addClass('btn-primary').prepend('<i class="ace-icon fa fa-check"></i>');
        buttons.eq(1).prepend('<i class="ace-icon fa fa-times"></i>')

        buttons = form.next().find('.navButton a');
        buttons.find('.ui-icon').hide();
        buttons.eq(0).append('<i class="ace-icon fa fa-chevron-left"></i>');
        buttons.eq(1).append('<i class="ace-icon fa fa-chevron-right"></i>');
    }

    function style_delete_form(form) {
        var buttons = form.next().find('.EditButton .fm-button');
        buttons.addClass('btn btn-sm btn-white btn-round').find('[class*="-icon"]').hide();//ui-icon, s-icon
        buttons.eq(0).addClass('btn-danger').prepend('<i class="ace-icon fa fa-trash-o"></i>');
        buttons.eq(1).addClass('btn-default').prepend('<i class="ace-icon fa fa-times"></i>')
    }

    function style_search_filters(form) {
        form.find('.delete-rule').val('X');
        form.find('.add-rule').addClass('btn btn-xs btn-primary');
        form.find('.add-group').addClass('btn btn-xs btn-success');
        form.find('.delete-group').addClass('btn btn-xs btn-danger');
    }

    function style_search_form(form) {
        var dialog = form.closest('.ui-jqdialog');
        var buttons = dialog.find('.EditTable')
        buttons.find('.EditButton a[id*="_reset"]').addClass('btn btn-sm btn-info').find('.ui-icon').attr('class', 'ace-icon fa fa-retweet');
        buttons.find('.EditButton a[id*="_query"]').addClass('btn btn-sm btn-inverse').find('.ui-icon').attr('class', 'ace-icon fa fa-comment-o');
        buttons.find('.EditButton a[id*="_search"]').addClass('btn btn-sm btn-purple').find('.ui-icon').attr('class', 'ace-icon fa fa-search');
    }

    function beforeDeleteCallback(e) {
        var form = $(e[0]);
        if (form.data('styled')) return false;

        form.closest('.ui-jqdialog').find('.ui-jqdialog-titlebar').wrapInner('<div class="widget-header" />')
        style_delete_form(form);

        form.data('styled', true);
    }

    function beforeEditCallback(e) {
        var form = $(e[0]);
        form.closest('.ui-jqdialog').find('.ui-jqdialog-titlebar').wrapInner('<div class="widget-header" />')
        style_edit_form(form);
    }


    // it causes some flicker when reloading or navigating grid
    // it may be possible to have some custom formatter to do this as the grid is being created to prevent this
    // or go back to default browser checkbox styles for the grid
    function styleCheckbox(table) {
        //
        // $(table).find('input:checkbox').addClass('ace')
        // .wrap('<label />')
        // .after('<span class="lbl align-top" />')
        //
        //
        // $('.ui-jqgrid-labels th[id*="_cb"]:first-child')
        // .find('input.cbox[type=checkbox]').addClass('ace')
        // .wrap('<label />').after('<span class="lbl align-top" />');
    }


    // 与导航按钮图标不同，行中的操作图标似乎是硬编码的，如果需要，可以在这里这样更改它们
    function updateActionIcons(table) {
        // var replacement =
        //     {
        //         'ui-ace-icon fa fa-pencil': 'ace-icon fa fa-pencil blue',
        //         'ui-ace-icon fa fa-trash-o': 'ace-icon fa fa-trash-o red',
        //         'ui-icon-disk': 'ace-icon fa fa-check green',
        //         'ui-icon-cancel': 'ace-icon fa fa-times red'
        //     };
        // $(table).find('.ui-pg-div span.ui-icon').each(function () {
        //     var icon = $(this);
        //     var $class = $.trim(icon.attr('class').replace('ui-icon', ''));
        //     if ($class in replacement) icon.attr('class', 'ui-icon ' + replacement[$class]);
        // })
    }

    //将图标替换为fontAwesome图标，如上面所示
    function updatePagerIcons(table) {
        var replacement =
            {
                'ui-icon-seek-first': 'ace-icon fa fa-angle-double-left bigger-140',
                'ui-icon-seek-prev': 'ace-icon fa fa-angle-left bigger-140',
                'ui-icon-seek-next': 'ace-icon fa fa-angle-right bigger-140',
                'ui-icon-seek-end': 'ace-icon fa fa-angle-double-right bigger-140'
            };
        $('.ui-pg-table:not(.navtable) > tbody > tr > .ui-pg-button > .ui-icon').each(function () {
            var icon = $(this);
            var $class = $.trim(icon.attr('class').replace('ui-icon', ''));

            if ($class in replacement) icon.attr('class', 'ui-icon ' + replacement[$class]);
        })
    }

    function enableTooltips(table) {
        $('.navtable .ui-pg-button').tooltip({container: 'body'});
        $(table).find('.ui-pg-div').tooltip({container: 'body'});
    }

    //var selr = jQuery(grid_selector).jqGrid('getGridParam','selrow');

    $(document).one('ajaxloadstart.page', function (e) {
        $.jgrid.gridDestroy(grid_selector);
        $('.ui-jqdialog').remove();
    });
});
