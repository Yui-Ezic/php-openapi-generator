<?php

namespace App\QuerySerializer\Transformer;

class ExplodeValues
{
    // Begins with '-' because real properties can't start with it
    public const string EXPLODE_FLAG = '-explode';

    public function __invoke(object $object, callable $next): mixed
    {
        $result = $next();

        if (!is_array($result)) {
            return $result;
        }

        $transformed = [];
        foreach ($result as $key => $value) {
            if ($key === self::EXPLODE_FLAG) {
                foreach ($value as $k => $v) {
                    $transformed[$k] = $v;
                }
                continue;
            }

            $transformed[$key] = $value;
        }
        return $transformed;
    }
}