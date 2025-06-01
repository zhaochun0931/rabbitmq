-module(tiny_kv).
-export([start/0, put/2, get/1, delete/1]).

start() ->
    ets:new(my_table, [named_table, public]).

put(Key, Value) ->
    ets:insert(my_table, {Key, Value}).

get(Key) ->
    case ets:lookup(my_table, Key) of
        [] -> not_found;
        [{_, Value}] -> Value
    end.

delete(Key) ->
    ets:delete(my_table, Key).
