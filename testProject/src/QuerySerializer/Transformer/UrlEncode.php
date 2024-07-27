<?php

namespace App\QuerySerializer\Transformer;

class UrlEncode
{
    public function __invoke(string $value, callable $next): string
    {
        return rawurlencode($value);
    }

}