<?php

namespace App\QuerySerializer\Transformer\Form;

readonly class ObjectExplode
{
    public function __invoke(object $object, callable $next): mixed
    {
        $result = $next();

        if (!is_array($result)) {
            return $result;
        }

        $exploded = [];

        foreach ($result as $key => $value) {
            if (is_array($value) && !array_is_list($value)) {
                // Remove object name, and explode object properties
                foreach ($value as $itemKey => $itemValue) {
                    $exploded[$itemKey] = $itemValue;
                }
            } else {
                $exploded[$key] = $value;
            }
        }

        return $exploded;

    }
}