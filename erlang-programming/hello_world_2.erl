-module(hello_world_2).
-include("hello_world_2.hrl").

-export([hello/0]).

-export([world/0]).


hello() ->
  "Hello xxx".

world() ->
  ?TEXT.
