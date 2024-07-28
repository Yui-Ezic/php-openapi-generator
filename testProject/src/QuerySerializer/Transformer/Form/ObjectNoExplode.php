<?php

namespace App\QuerySerializer\Transformer\Form;

class ObjectNoExplode
{
    public function __invoke(object $object, callable $next): mixed
    {
        $result = $next();

        if (!is_array($result)) {
            return $result;
        }

        $newResult = [];

        foreach ($result as $key => $value) {
            if (is_array($value) && !array_is_list($value)) {
                $newValue = '';
                foreach ($value as $itemKey => $itemValue) {
                    $newValue .= $itemKey . ',' . $itemValue . ',';
                }
                $newResult[$key] = substr($newValue, 0, -1);
            } else {
                $newResult[$key] = $value;
            }
        }

        return $newResult;
    }
}