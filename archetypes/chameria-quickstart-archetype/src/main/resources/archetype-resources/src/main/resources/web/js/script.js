$(function(){

  $('ul.hover_block li').hover(function(){
    $(this).find('img').animate({top:'182px'},{queue:false,duration:500});
  }, function(){
    $(this).find('img').animate({top:'0px'},{queue:false,duration:500});
  });
});