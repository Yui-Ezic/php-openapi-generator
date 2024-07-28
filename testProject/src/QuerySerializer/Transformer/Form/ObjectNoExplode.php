<?php

namespace App\QuerySerializer\Transformer\Form;

use Attribute;
use CuyZ\Valinor\Normalizer\AsTransformer;

#[AsTransformer]
#[Attribute(Attribute::TARGET_PROPERTY)]
class ObjectNoExplode
{
    public function normalize(object $object, callable $next): mixed
    {
        $result = $next();

        if (!is_array($result)) {
            return $result;
        }

        $newValue = '';
        foreach ($result as $key => $value) {
            $newValue .= $key . ',' . $value . ',';
        }
        return substr($newValue, 0, -1);
    }
}