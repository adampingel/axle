
This is part of a larger project on source code
search algorithms.

python2json.py will take any python 2.6 (or older)
file and return a json document that represents the
abstract syntax tree.  There are a couple of minor
problems with it, but for the most part it works.

Feel free to submit bug patches to <pingel@gmail.com>.

As an example, let's say we have the following python in
example.py:

    x = 1 + 2
    print x

Invoke the script like so to turn example.py into json:

    python2json.py -f example.py

You can also provide the input via stdin:

    cat example.py | python2json.py

I find it useful to chain this pretty-printer when debugging:

    cat example.py | python2json.py | python -mjson.tool

The pretty-printed result in this case is:

{
    "_lineno": null, 
    "node": {
        "_lineno": null, 
        "spread": [
            {
                "_lineno": 2, 
                "expr": {
                    "_lineno": 2, 
                    "left": {
                        "_lineno": 2, 
                        "type": "Const", 
                        "value": "1"
                    }, 
                    "right": {
                        "_lineno": 2, 
                        "type": "Const", 
                        "value": "2"
                    }, 
                    "type": "Add"
                }, 
                "nodes": [
                    {
                        "_lineno": 2, 
                        "name": "x", 
                        "type": "AssName"
                    }
                ], 
                "type": "Assign"
            }, 
            {
                "_lineno": 3, 
                "nodes": [
                    {
                        "_lineno": 3, 
                        "name": "x", 
                        "type": "Name"
                    }
                ], 
                "type": "Printnl"
            }
        ], 
        "type": "Stmt"
    }, 
    "type": "Module"
}
