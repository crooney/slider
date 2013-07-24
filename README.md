# slider

A simple Clojurescript library to implement interactive slideshows. Slides may be images, divs or anything else.

A page may have several slideshows, and ids may be repeated in different slideshows. The delay between slides and the time of transition may be specified in HTML or in code. Different transitions may be coded.

slider uses core.async to avoid callbacks and enfocus for DOM manipulation.

## Usage

Slideshows should be a relatively positioned div with class `slider`. It should have as children an arbitrary number of absolutely positioned elements of class `pane`. If backward and forward buttons are desired, the should have the class `button`. The first `button` is back and the second forward.

To start automatically, slideshows need the slider div to have `data-pause` and `data-trans-time` attributes, which specify the delay and transition times in milliseconds. The only script then necessary for any amount of slideshows is `<script>window.onload=crooney.slider.start_all</script>`.

To start individually through javascript the call is `crooney.slider.start(` *id,delay,transition-time* `)`.

## Example

The following makes a 4 image slideshow with 4.8 seconds between slides and .8 seconds for transition. Note that transition time is included in the delay: total time is 4.8 not 5.6.
```html
<div id="octo" class="slider" data-pause="4800" data-trans-time="800">
    <img id="u7"  class="pane" src="//octodex.github.com/images/total-eclipse-of-the-octocat.jpg" style="opacity: 1;"/>
    <img id="u8"  class="pane" src="//octodex.github.com/images/scottocat.jpg" />
    <img id="u9"  class="pane" src="//octodex.github.com/images/octonaut.jpg" />
    <img id="u10"  class="pane" src="//octodex.github.com/images/pacman-ghosts.jpg" />
    <div id="leftbutton" class="button" style="left:0%">back</div>
    <div id="rightbutton" class="button" style="left:80%">ahead</div>
</div>
<script src="js/slider.js"></script>
<script>window.onload=crooney.slider.start_all</script>
```
Note that it is recommended to include the script after all of the slideshows to avoid problems with google closure's optimizations.

Cheers.

## License

Copyright © 2013 Christopher Rooney

Distributed under the Eclipse Public License, the same as Clojure.
