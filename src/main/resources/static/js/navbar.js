function getNav(){
  var content =  '<div class="navbar-wrapper ">'
                  + '<div class="container ">'
                    +'<nav class="navbar navbar-inverse navbar-static-top navbar-custom ">'
                      +'<div class="container">'
                        +'<div class="navbar-header">'
                        +  '<a class="navbar-brand navbar-custom" style = "cursor: pointer; color: white; border-top-left-radius: 10px; border-bottom-left-radius: 10px;" onclick = "sendToPage(\'\')">Cyplanner</a>'
                      +  '</div>'
                      +  '<div id="navbar" class="navbar-collapse collapse">'
                        +  '<ul id = "addUser" class="nav navbar-nav navbar-custom">'
                          +  '<li class="">'
                              +'<a href="#" class= "dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Create Schedule <span class="caret"></span></a>'
                              +'<ul class="dropdown-menu">'
                              +  '<li style = "cursor: pointer;"><a onclick = "sendToPage(\'manage\')">Manual Entry</a></li>'
                              +  '<li style = "cursor: pointer;"><a onclick = "sendToPage(\'Auto\')">Automatic Entry</a></li>'
                            +  '</ul>'
                          +  '</li>'
                          +  '<li class="">'
                            +  '<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Course Ratings <span class="caret"></span></a>'
                              +'<ul class="dropdown-menu">'
                              +  '<li style = "cursor: pointer;"><a onclick = "sendToPage(\'view\')">View Ratings</a></li>'
                              +  '<li style = "cursor: pointer;"><a onclick = "sendToPage(\'rate\')">Rate a Course</a></li>'
                              +'</ul>'
                          +  '</li>'
                            +'<li class="" style = "cursor: pointer; " ><a style = "color: white;" onclick="sendToPage(\'Departments\')">Departments</a></li>'
                            +'<li id = "admin" onclick="sendToPage(\'admin\')" class="test" style = "cursor: pointer;" ></li>'
                          +'</ul>'
                        +'</div>'
                      +'</div>'
                    +'</nav>'
                  +'</div>'
                +'</div>';
                return content;
}

function getLogin(){
  var content = '<a class = "signup" id = "login" onclick = "sendToPage(\'Login\')"></a>'
         +  ' <a class = "signup" id = "signup" onclick = "sendToPage(\'SignUp\')"></a>';
         return content;
}

function checkUser(){

  if(localStorage.getItem("user") == "admin"){
    $("#admin").html("<a>Admin</a>");
  }
  if(localStorage.getItem("user") !== null){
    $("#user1").html("<a>Completed Courses</a>");
    $("#user2").html("<a>Saved Schedules</a>");
    $("#addUser").html($("#addUser").html() + '<li class="">'
    + '<a href="#" class= "dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">'
    + localStorage.getItem("user") + '<span class="caret"></span></a>'
    + '<ul class="dropdown-menu">'
    + '<li class = "" style = "cursor: pointer;"><a onclick = "sendToPage(\'profile\')">profile</a></li>'
    + '<li class = "" style = "cursor: pointer;"><a onclick = "logout(\'\')">logout</a></li>'
    + '</ul></li>');
    $(".login").html("");
  }
  else{
    $("#login").html("<strong>Login</strong>");
    $("#signup").html("<strong>SignUp</strong>");
  }
}

function logout(id){
  localStorage.clear();
  if(document.URL.includes('localhost')){
    window.location.href = "http://localhost:8080/" + id;
  }
  else{
    window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/" + id;
  }
}

function sendToPage(id){
  if(document.URL.includes('localhost')){
    window.location.href = "http://localhost:8080/" + id;
  }
  else{
    window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/" + id;
  }
}
