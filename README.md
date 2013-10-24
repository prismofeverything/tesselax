# tesselax

Automatic animated layouts in clojurescript

## Usage

To simply run it:

    % lein cljsbuild once
    % lein ring server
    
Rectangles!

In order to interact with the running console from brepl you need three separate terminals:

    % lein cljsbuild auto
    % lein ring server
    % lein cljsbuild repl-listen

Now type some things in the brepl and it will magically happen in the browser!

## License

Copyright © 2013 Ryan Spangler

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
