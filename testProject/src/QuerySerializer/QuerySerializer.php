<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Transformer\Form;
use App\QuerySerializer\Transformer\UrlEncode;
use CuyZ\Valinor\MapperBuilder;
use CuyZ\Valinor\Normalizer\Format;

final readonly class QuerySerializer
{
    public function serialize(object $query): string
    {
        // TODO: Support "allowReserved: true"
        $normalizer = (new MapperBuilder())
            ->registerTransformer(new UrlEncode())
            ->registerTransformer(new Form\ArrayExplode())
            ->registerTransformer(new Form\ObjectExplode())
            ->normalizer(Format::array());

        $array = $normalizer->normalize($query);

        $arrayForImplode = [];
        foreach ($array as $key => $value) {
            $arrayForImplode[$key] = $key . '=' . $value;
        }

        return implode("&", $arrayForImplode);
    }
}