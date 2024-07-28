<?php

namespace App\QuerySerializer\Transformer\Form;

class ArrayNoExplode
{
    public function __invoke(array $array, callable $next): mixed
    {
        if (array_is_list($array)) {
            return implode(',', $next());
        }
        return $next();
    }

}