
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



var entries = [
                { label: 'Stupid Hackathon New York', url: 'https://stupidhackathon.com', target: '_blank' },
                { label: 'Stupid Hackathon Toronto', url: 'http://stupidhacktoronto.com', target: '_blank' },
                { label: 'Stupid Hackathon London', url: 'https://www.stupidhackathon.uk/', target: '_blank' },
                { label: 'Countdown', url: 'https://www.jqueryscript.net/tags.php?/countdown/', target: '_top' },
                { label: 'Dropdown Menu', url: 'https://www.jqueryscript.net/tags.php?/Drop%20Down%20Menu/', target: '_top' },
                { label: 'CodePen', url: 'https://codepen.io/', target: '_top' },
								{ label: 'Twitter', url: 'https://twitter.com/niklaswebdev', target: '_top' },
			{ label: 'deviantART', url: 'http://nkunited.deviantart.com/', target: '_top' },
			{ label: 'Gulp', url: 'http://gulpjs.com/', target: '_top' },
			{ label: 'Browsersync', url: 'https://www.browsersync.io/', target: '_top' },
			{ label: 'GitHub', url: 'https://github.com/', target: '_top' },
			{ label: 'Shadertoy', url: 'https://www.shadertoy.com/', target: '_top' },
			{ label: 'Tree View', url: 'https://www.jqueryscript.net/tags.php?/tree%20view/', target: '_top' },
			{ label: 'jsPerf', url: 'http://jsperf.com/', target: '_top' },
			{ label: 'Foundation', url: 'https://foundation.zurb.com/', target: '_top' },
			{ label: 'CreateJS', url: 'https://createjs.com/', target: '_top' },
			{ label: 'Velocity.js', url: 'http://julian.com/research/velocity/', target: '_top' },
			{ label: 'TweenLite', url: 'https://greensock.com/docs/#/HTML5/GSAP/TweenLite/', target: '_top' },
			{ label: 'jQuery', url: 'https://jquery.com/', target: '_top' },
			{ label: 'Notification', url: 'https://www.jqueryscript.net/tags.php?/Notification/', target: '_top' },
			{ label: 'Parallax', url: 'https://www.jqueryscript.net/tags.php?/parallax/', target: '_top' }
            ];

            var settings = {

                entries: entries,
                width: 640,
                height: 480,
                radius: '50%',
                radiusMin: 75,
                bgDraw: true,
                bgColor: '#000',
                opacityOver: 1.00,
                opacityOut: 0.05,
                opacitySpeed: 6,
                fov: 800,
                speed: 0.5,
                fontFamily: 'Oswald, Arial, sans-serif',
                fontSize: '15',
                fontColor: '#fbd62a',
                fontWeight: 'normal',//bold
                fontStyle: 'normal',//italic
                fontStretch: 'normal',//wider, narrower, ultra-condensed, extra-condensed, condensed, semi-condensed, semi-expanded, expanded, extra-expanded, ultra-expanded
                fontToUpperCase: true

            };

            //var svg3DTagCloud = new SVG3DTagCloud( document.getElementById( 'holder'  ), settings );
            $( '#inspiration-cloud' ).svg3DTagCloud( settings );

});
