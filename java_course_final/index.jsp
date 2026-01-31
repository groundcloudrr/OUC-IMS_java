<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>媒体服务器 - 文件上传</title>
    <!-- 引入Bootstrap CSS -->
    <link href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <!-- 导航栏 -->
    <nav class="navbar navbar-inverse navbar-fixed-top">
        <div class="container">
            <div class="navbar-header">
                <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">媒体服务器</a>
            </div>
            <div id="navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <li class="active"><a href="${pageContext.request.contextPath}/index.jsp">上传文件</a></li>
                    <li><a href="${pageContext.request.contextPath}/fileList">文件列表</a></li>
                    <li><a href="${pageContext.request.contextPath}/rank">热度排行</a></li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="${pageContext.request.contextPath}/logout">退出登录</a></li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- 主体内容 -->
    <div class="container" style="margin-top: 80px;">
        <div class="row">
            <div class="col-md-8 col-md-offset-2">
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">媒体文件上传</h3>
                    </div>
                    <div class="panel-body">
                        <!-- 上传表单 -->
                        <form id="uploadForm" action="${pageContext.request.contextPath}/upload" method="post" enctype="multipart/form-data">
                            <div class="form-group">
                                <label for="file">选择文件（支持jpg、png、gif、mp3、mp4，单文件最大500MB）</label>
                                <input type="file" id="file" name="file" multiple class="form-control">
                            </div>
                            <!-- 上传进度条（默认隐藏） -->
                            <div class="progress" style="display: none; margin-top: 20px;">
                                <div class="progress-bar progress-bar-success progress-bar-striped active" 
                                     role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" 
                                     style="width: 0%">
                                    0%
                                </div>
                            </div>
                            <button type="submit" class="btn btn-primary btn-block" id="uploadBtn">开始上传</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 引入jQuery和Bootstrap JS -->
    <script src="https://cdn.bootcss.net/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/bootstrap/js/bootstrap.min.js"></script>

    <!-- 上传进度条交互脚本 -->
    <script>
        $(function() {
            $('#uploadForm').submit(function(e) {
                e.preventDefault(); // 阻止表单默认提交（使用AJAX异步上传）

                // 检查是否选择文件
                var fileInput = $('#file')[0];
                if (fileInput.files.length === 0) {
                    alert('请选择要上传的文件！');
                    return;
                }

                // 显示进度条
                var progressBar = $('.progress');
                var progressBarInner = $('.progress-bar');
                progressBar.show();
                progressBarInner.css('width', '0%').text('0%');

                // 构建FormData对象（包含上传文件数据）
                var formData = new FormData(this);

                // 发起AJAX异步上传请求
                $.ajax({
                    url: $(this).attr('action'),
                    type: 'post',
                    data: formData,
                    processData: false, // 禁止jQuery处理数据（避免文件数据被破坏）
                    contentType: false, // 禁止jQuery设置Content-Type（由浏览器自动处理）
                    xhr: function() {
                        // 自定义XHR对象，监听上传进度
                        var xhr = $.ajaxSettings.xhr();
                        if (xhr.upload) {
                            xhr.upload.addEventListener('progress', function(e) {
                                if (e.lengthComputable) {
                                    // 计算上传进度：(已上传字节 / 总字节) * 100%
                                    var percent = (e.loaded / e.total) * 100;
                                    progressBarInner.css('width', percent + '%').text(Math.round(percent) + '%');
                                }
                            }, false);
                        }
                        return xhr;
                    },
                    success: function(data) {
                        // 执行返回的脚本（跳转/提示）
                        var script = data.match(/<script>([\s\S]*?)<\/script>/)[1];
                        eval(script);
                    },
                    error: function() {
                        alert('上传失败，请重试！');
                        progressBar.hide();
                    }
                });
            });
        });
    </script>
</body>
</html>