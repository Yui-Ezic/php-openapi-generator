<?php

namespace App\QuerySerializer\Transformer\Form;

readonly class ArrayExplode
{
    public function __construct(
        private string $delimiter = '&'
    )
    {
    }

    public function __invoke(object $object, callable $next): mixed
    {
        $result = $next();

        if (!is_array($result)) {
            return $result;
        }

        $exploded = [];

        foreach ($result as $key => $value) {
            $explodedValue = null;

            if (is_array($value) && array_is_list($value)) {
                foreach ($value as $valueItem) {
                    if ($explodedValue === null) {
                        // Skip key in first value, cause this key will be added by normalizer
                        $explodedValue = $valueItem;
                    } else {
                        $explodedValue .= $this->delimiter . $key . '=' . $valueItem;
                    }
                }
            }

            $exploded[$key] = $explodedValue !== null ? $explodedValue : $value;
        }

        return $exploded;

    }
}