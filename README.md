# slider

A simple Clojurescript library to implement interactive slideshows. Slides may be images, divs or anything else.

A page may have several slideshows, and ids may be repeated in different slideshows. The delay between slides and the time of transmission may be specified in HTML or in code. Different transitions may be coded.


## Usage

Slideshows should be a relativelt positioned div with class `slider`. It should have as children an arbitrary number of absolutely positioned elements of class `pane`. If backward and forward buttons are desired, the should have the class `button`. The first `button` is back and the second forward.

To start automatically, slideshows need the slider div to have `data-pause` and `data-trans-time` attributes, which specify the delay and transition times in milliseconds. The only script then necessary for any amount of slideshows is `<script>window.onload=crooney.slider.start_all</script>`.

To start individually through javascript the call is `crooney.slider.start(`*id,delay,transition-time*`)`.

## License

Copyright © 2013 Christopher Rooney

Distributed under the Eclipse Public License, the same as Clojure.
