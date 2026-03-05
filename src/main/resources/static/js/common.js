// =================== 定义全局变量 ======================
let avatarUrl = 'image/avatar01.jpeg'; // 默认头像
let currentArticle; // 当前访问的帖子
let currentUserId;  // 当前登录用户
let profileUserId;  // 查看个人信息

// =================== 登录状态检查函数 ======================
/**
 * 检查用户登录状态，如果未登录则跳转到登录页
 * @param {string} redirectUrl - 可选，跳转后的重定向地址（默认为 sign-in.html）
 */
function checkLoginStatus(redirectUrl) {
    const token = localStorage.getItem('token');
    if (!token) {
        console.log('未检测到登录 token，跳转到登录页');
        window.location.href = redirectUrl || 'sign-in.html';
        return false;
    }
    return true;
}

/**
 * 处理登录失败，清除 token 并跳转
 * @param {string} redirectUrl - 可选，跳转后的重定向地址
 */
function handleLoginFailure(redirectUrl) {
    console.log('登录已失效，跳转到登录页');
    localStorage.removeItem('token');
    window.location.href = redirectUrl || 'sign-in.html';
}


// ============================ 处理导航激活效果 ===========================
function changeNavActive (boardItem) {

    // 每次切换版块 / 回到首页时，清空搜索关键字
    if ($('#index_search_input').length > 0) {
        $('#index_search_input').val('');
        console.log("已经清空关键字");
    }

    // 判断当前是否为激活状态
    if (boardItem.hasClass('active') == false) {
      let activeLiEl = $('#topBoardList>.active');
      activeLiEl.removeClass('active');
      boardItem.addClass('active');
      // 请求版块中的帖子
      buildArticleList();
    }
}

// ============================ 删除导航激活效果 ===========================
function removeNavActive () {
    // 判断当前是否为激活状态
    let activeLiEl = $('#topBoardList>.active');
    if (activeLiEl) {
      activeLiEl.removeClass('active');
    }
}

//======================= 处理导航栏点击并获取帖子列表 ======================
function buildArticleList (){
  console.log('发送请求查询帖子列表');
  $('#bit-forum-content').load('article_list.html');
}

// 设置站内信接收用户信息
function setMessageReceiveUserInfo (userId, nickname) {
  console.log('userId = ' + userId);
  console.log('nickname = ' + nickname);
  $('#index_message_receive_user_id').val(userId);
  $('#index_message_receive_user_name').html('发送给: <strong>' + nickname + '</strong>');
  console.log('value = ' + $('#index_message_receive_user_id').val());
}

//