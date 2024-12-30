package com.quaxt.parser;

import com.quaxt.Token;

public record Function(String name, Token returnType, Node statement) {
}

