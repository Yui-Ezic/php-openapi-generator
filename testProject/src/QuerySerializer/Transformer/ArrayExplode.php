<?php

namespace App\QuerySerializer\Transformer;

use Attribute;
use CuyZ\Valinor\Normalizer\AsTransformer;

/**
 * Array serialization with explode exactly same for all styles, so this class on general namespace
 * @see https://swagger.io/docs/specification/serialization/
 */
#[AsTransformer]
#[Attribute(Attribute::TARGET_PROPERTY)]
readonly class ArrayExplode
{
    public function __construct(
        // TODO: Stupid hack because idk how to get key value in normalize() method.
        private string $key,
        private string $delimiter = '&',
    )
    {
    }

    /**
     * @param non-empty-list $array
     */
    public function normalize(array $array, callable $next): string
    {
        $result = null;

        foreach ($next() as $value) {
            if ($result === null) {
                // Skip key in first value, cause this key will be added by normalizer
                $result = $value;
            } else {
                $result .= $this->delimiter . $this->key . '=' . $value;
            }
        }

        return $result;

    }
}