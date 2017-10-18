
// jQuery on document ready
	$(document).ready(function(){

/* dramatic flare in the header */
   $('#action-block').on('mouseover', function() {
     $('#dramatic-sound')[0].volume = 0.2;
    $('#dramatic-sound')[0].load();
     $('#dramatic-sound')[0].play();
   });


   $('#action-block').on('mouseout', function () {
      $('#dramatic-sound')[0].pause();
      $('#dramatic-sound')[0].currentTime = 0;
   });

/* countdown */
function timer() {


  var then = new Date(2017, 11, 11, 9, 0, 0, 0);
  var now = new Date();

  if(now.getTime() > then.getTime()) {
    return;
  }
  var dif = then.getTime() - now.getTime();

  var seconds = Math.floor(dif / 1000);
  $('#seconds').html(seconds);
}
setInterval(timer, 1000);
timer();


$('[data-toggle="tooltip"]').tooltip();
});
